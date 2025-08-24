package com.github.dewarepk.keytron.repo;

import com.github.dewarepk.keytron.model.License;
import com.github.dewarepk.keytron.model.LicenseStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface LicenseRepo extends MongoRepository<License, String> {

    Optional<License> findByKeyHashAndStatus(String keyHash, LicenseStatus status);
    long countByStatusAndExpiresAtBefore(LicenseStatus status, Instant before);
}
