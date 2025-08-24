package com.github.dewarepk.keytron.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document("licenses")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class License {

    @Id
    private String id;

    @Indexed
    private String productId;

    @Indexed(unique = true)
    private String keyHash;

    @Builder.Default
    private LicenseStatus status = LicenseStatus.ACTIVE;

    private String plan;

    @Builder.Default
    private int seats = 1;

    private Instant firstActivatedAt;

    @Indexed
    private Instant lastValidatedAt;

    @Indexed
    private Instant expiresAt;

    private String initialDeviceFingerprint;

    private String notes;

    @CreatedDate
    private Instant createdAt;

    private Instant revokedAt;

    @LastModifiedDate
    private Instant updatedAt;

}
