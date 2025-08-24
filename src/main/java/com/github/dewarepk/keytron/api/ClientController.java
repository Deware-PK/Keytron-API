package com.github.dewarepk.keytron.api;

import com.github.dewarepk.keytron.service.LicenseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/v1/licenses")
@RequiredArgsConstructor
public class ClientController {

    private final LicenseService service;

    @PostMapping("/activate")
    public ActivateResponse activate(@RequestBody ActivateRequest req, HttpServletRequest http) {
        System.out.println("DEBUG >>> req.licenseKey = " + req.licenseKey());
        var r = service.activate(new LicenseService.ActivateReq(
                req.licenseKey(), req.deviceFingerprint(), req.os(), req.hostname(), req.appVersion(),
                http.getRemoteAddr(), http.getHeader("User-Agent")));
        return new ActivateResponse(r.activationId(), r.token(), r.plan(), r.seats(), r.expiresAt());
    }

    @GetMapping("/validate")
    public ValidateResponse validate(org.springframework.security.core.Authentication auth) {
        @SuppressWarnings("unchecked")
        var details = (Map<String,Object>) auth.getDetails();
        service.touchValidate((String) details.get("lic"), (String) details.get("act"));
        return new ValidateResponse(true, null, Instant.now());
    }

    @PostMapping("/heartbeat")
    public void heartbeat(@RequestBody HeartbeatRequest req, HttpServletRequest http) {
        service.heartbeat(req.activationId(), http.getRemoteAddr(), http.getHeader("User-Agent"));
    }

    @PostMapping("/deactivate")
    public void deactivate(@RequestBody DeactivateRequest req, HttpServletRequest http) {
        service.deactivate(req.activationId(), http.getRemoteAddr(), http.getHeader("User-Agent"));
    }

    public record ActivateRequest(
            String licenseKey,
            String deviceFingerprint,
            String os,
            String hostname,
            String appVersion,
            String ip,
            String userAgent
    ) {}


    public record ActivateResponse(String activationId, String token, String plan, int seats, Instant expiresAt) {}

    public record HeartbeatRequest(String activationId) {}

    public record DeactivateRequest(String activationId) {}

    public record ValidateResponse(boolean valid, String reason, Instant now) {}
}
