package com.github.dewarepk.keytron.api;


import com.github.dewarepk.keytron.model.License;
import com.github.dewarepk.keytron.model.LicenseStatus;
import com.github.dewarepk.keytron.repo.LicenseRepo;
import com.github.dewarepk.keytron.repo.ProductRepo;
import com.github.dewarepk.keytron.util.LicenseKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/v1/admin/licenses")
@RequiredArgsConstructor
public class AdminController {

    private final LicenseRepo licenseRepo;
    private final ProductRepo productRepo;
    private final LicenseKeyUtil keyUtil;

    @PostMapping("/generate")
    public GenResp generate(@RequestBody GenReq req) {
        var code = req.productCode().toUpperCase(); // กันเคสเล็ก/ใหญ่
        var product = productRepo.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unknown productCode: " + code));

        if (req.seats() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seats must be > 0");
        }
        if (req.expiresAt() == null || req.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expiresAt must be in the future (UTC)");
        }

        var plainKey = keyUtil.generateKey(req.plan());
        var lic = licenseRepo.save(License.builder()
                .productId(product.getId())
                .keyHash(keyUtil.hashKey(plainKey))
                .plan(req.plan()).seats(req.seats())
                .status(LicenseStatus.ACTIVE)
                .expiresAt(req.expiresAt())
                .createdAt(Instant.now())
                .build());
        return new GenResp(lic.getId(), plainKey);
    }


    @PostMapping("/ban")
    public void ban(@RequestBody BanReq req) {
        var lic = licenseRepo.findById(req.licenseId()).orElseThrow();
        lic.setStatus(LicenseStatus.BANNED);
        lic.setRevokedAt(Instant.now());
        licenseRepo.save(lic);
    }


    public record GenReq(String productCode, String plan, int seats, Instant expiresAt) {}

    public record GenResp(String licenseId, String licenseKeyPlain) {}

    public record BanReq(String licenseId, String reason) {}
}
