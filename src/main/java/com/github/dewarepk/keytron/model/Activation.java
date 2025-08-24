package com.github.dewarepk.keytron.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document("activations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "uniq_license_device", def = "{'licenseId': 1, 'deviceFingerprint': 1}", unique = true),
        @CompoundIndex(name = "by_license_lastSeen", def = "{'licenseId': 1, 'lastSeenAt': -1}")
})
public class Activation {

    @Id
    private String id;

    @Indexed
    private String licenseId;           // FK -> licenses._id

    private String deviceFingerprint;   // เช่น "sha256:...."

    /** meta ของเครื่อง/แอป (เลือกเก็บได้) */
    private DeviceMeta deviceMeta;

    @CreatedDate
    private Instant createdAt;          // เวลา activate ครั้งแรกของเครื่องนี้

    /** เวลา validate/heartbeat ล่าสุด */
    private Instant lastSeenAt;

    @Indexed(name = "activationExpiresAt_ttl")
    private Instant expiresAt;

    private Instant revokedAt;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeviceMeta {
        private String os;          // Windows 10, macOS 14, ...
        private String hostname;    // DESKTOP-XXXX
        private String appVersion;  // 1.0.3
        private String ip;          // optional
        private String userAgent;   // optional
    }

}
