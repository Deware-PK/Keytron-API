package com.github.dewarepk.keytron.repo;

import com.github.dewarepk.keytron.model.Activation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ActivationRepo extends MongoRepository<Activation, String> {
    long countByLicenseIdAndRevokedAtIsNull(String licenseId);
    Optional<Activation> findByLicenseIdAndDeviceFingerprintAndRevokedAtIsNull(String licenseId, String device);
    List<Activation> findByLicenseId(String licenseId);
}