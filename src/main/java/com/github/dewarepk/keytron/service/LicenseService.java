package com.github.dewarepk.keytron.service;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dewarepk.keytron.model.*;
import com.github.dewarepk.keytron.repo.ActivationRepo;
import com.github.dewarepk.keytron.repo.AuditLogRepo;
import com.github.dewarepk.keytron.repo.LicenseRepo;
import com.github.dewarepk.keytron.repo.ProductRepo;
import com.github.dewarepk.keytron.util.LicenseKeyUtil;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepo licenseRepo;
    private final ActivationRepo activationRepo;
    private final ProductRepo productRepo;
    private final AuditLogRepo auditRepo;
    private final TokenService tokenService;
    private final LicenseKeyUtil keyUtil;

    @Value("${app.license.activation.heartbeatTtlMinutes:60}")
    protected long ttlMinutes;

    public ActivateResp activate(ActivateReq req) {

        System.out.println("MAPPED licenseKey = " + req.licenseKey());

        String hash = keyUtil.hashKey(req.licenseKey());
        License lic = licenseRepo.findByKeyHashAndStatus(hash, LicenseStatus.ACTIVE)
                .orElseThrow(() -> deny(HttpStatus.NOT_FOUND, "INVALID_KEY | " + "DEBUG hashKey=" + hash + "\n" + "DEBUG rawKey=" + req.licenseKey() + "\n" + "DEBUG normKey=" + keyUtil.normalize(req.licenseKey())));


        if (lic.getExpiresAt() != null && lic.getExpiresAt().isBefore(Instant.now()))
            throw deny(HttpStatus.FORBIDDEN, "EXPIRED");
        if (lic.getStatus() == LicenseStatus.BANNED || lic.getRevokedAt() != null)
            throw deny(HttpStatus.FORBIDDEN, "BANNED");

        if (lic.getInitialDeviceFingerprint() != null &&
                !lic.getInitialDeviceFingerprint().equals(req.deviceFingerprint())) {
            writeAudit(lic.getId(), null, req, AuditType.ACTIVATE_FAIL, "HWID_MISMATCH");
            throw deny(HttpStatus.FORBIDDEN, "HWID_MISMATCH");
        }

        long inUse = activationRepo.countByLicenseIdAndRevokedAtIsNull(lic.getId());
        Optional<Activation> existing = activationRepo.findByLicenseIdAndDeviceFingerprintAndRevokedAtIsNull(lic.getId(), req.deviceFingerprint());
        if (existing.isEmpty() && inUse >= lic.getSeats()) {
            writeAudit(lic.getId(), null, req, AuditType.ACTIVATE_FAIL, "NO_SEATS");
            throw deny(HttpStatus.FORBIDDEN, "NO_SEATS");
        }

        Activation act = existing.orElseGet(() -> {
           Instant now = Instant.now();
           Activation.DeviceMeta deviceMeta = Activation.DeviceMeta.builder()
                   .os(req.os())
                   .hostname(req.hostname())
                   .appVersion(req.appVersion())
                   .ip(req.ip())
                   .userAgent(req.userAgent())
                   .build();
            Activation a = Activation.builder()
                    .licenseId(lic.getId())
                    .deviceFingerprint(req.deviceFingerprint())
                    .deviceMeta(deviceMeta)
                    .createdAt(now).lastSeenAt(now)
                    .expiresAt(now.plus(Duration.ofMinutes(ttlMinutes)))
                    .build();
            try { return activationRepo.save(a); }
            catch (DuplicateKeyException d) {

                return activationRepo.findByLicenseIdAndDeviceFingerprintAndRevokedAtIsNull(lic.getId(), req.deviceFingerprint()).orElseThrow();
            }
        });

        if (lic.getFirstActivatedAt() == null) lic.setFirstActivatedAt(Instant.now());
        if (lic.getInitialDeviceFingerprint() == null) lic.setInitialDeviceFingerprint(req.deviceFingerprint());
        lic.setLastValidatedAt(Instant.now());
        licenseRepo.save(lic);

        Product product = productRepo.findById(lic.getProductId()).orElse(null);
        String token = tokenService.issue(lic.getId(), act.getId(),
                product != null ? product.getCode() : "UNKNOWN", lic.getPlan());

        writeAudit(lic.getId(), act.getId(), req, AuditType.ACTIVATE_OK, null);
        return new ActivateResp(act.getId(), token, lic.getPlan(), lic.getSeats(), act.getExpiresAt());

    }

    public void heartbeat(String activationId, String ip, String ua) {
        var act = activationRepo.findById(activationId)
                .orElseThrow(() -> deny(HttpStatus.NOT_FOUND, "ACT_NOT_FOUND"));
        if (act.getRevokedAt() != null) throw deny(HttpStatus.FORBIDDEN, "ACT_REVOKED");
        var lic = licenseRepo.findById(act.getLicenseId()).orElseThrow();
        if (lic.getStatus() != LicenseStatus.ACTIVE) throw deny(HttpStatus.FORBIDDEN, "LICENSE_INACTIVE");

        Instant now = Instant.now();
        act.setLastSeenAt(now);
        act.setExpiresAt(now.plus(Duration.ofMinutes(ttlMinutes)));
        activationRepo.save(act);

        lic.setLastValidatedAt(now);
        licenseRepo.save(lic);

        writeAudit(lic.getId(), act.getId(),
                new ActivateReq(null, act.getDeviceFingerprint(), null, null, null, ip, ua),
                AuditType.HEARTBEAT, null);
    }

    public void deactivate(String activationId, String ip, String ua) {
        var act = activationRepo.findById(activationId)
                .orElseThrow(() -> deny(HttpStatus.NOT_FOUND, "ACT_NOT_FOUND"));
        if (act.getRevokedAt() == null) {
            act.setRevokedAt(Instant.now());
            activationRepo.save(act);
        }
        writeAudit(act.getLicenseId(), act.getId(),
                new ActivateReq(null, act.getDeviceFingerprint(), null, null, null, ip, ua),
                AuditType.DEACTIVATE, null);
    }

    public void touchValidate(String licenseId, String activationId) {
        // เรียกตอน /validate สำเร็จ เพื่ออัปเดต lastSeen/lastValidated
        var now = Instant.now();
        licenseRepo.findById(licenseId).ifPresent(l -> { l.setLastValidatedAt(now); licenseRepo.save(l); });
        activationRepo.findById(activationId).ifPresent(a -> { a.setLastSeenAt(now); activationRepo.save(a); });
    }

    private ResponseStatusException deny(HttpStatus code, String reason) {
        return new ResponseStatusException(code, reason);
    }

    private void writeAudit(String licId, String actId, ActivateReq req, AuditType type, String reason) {
        AuditLog log = AuditLog.builder()
                .ts(Instant.now())
                .type(type)
                .licenseId(licId)
                .activationId(actId)
                .deviceFingerprint(req.deviceFingerprint())
                .ip(req.ip()).userAgent(req.userAgent())
                .reason(reason)
                .build();
        auditRepo.save(log);
    }

    public record ActivateReq(
            @JsonProperty("licenseKey") String licenseKey,
            @JsonProperty("deviceFingerprint") String deviceFingerprint,
            @JsonProperty("os") String os,
            @JsonProperty("hostname") String hostname,
            @JsonProperty("appVersion") String appVersion,
            @JsonProperty("ip") String ip,
            @JsonProperty("userAgent") String userAgent
    ) {}


    public record ActivateResp(String activationId, String token, String plan,
                               int seats, Instant expiresAt) {}
}
