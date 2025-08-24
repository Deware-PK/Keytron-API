package com.github.dewarepk.keytron.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document("audit_logs")
@CompoundIndexes({
        @CompoundIndex(name = "by_license_ts", def = "{'licenseId': 1, 'ts': -1}"),
        @CompoundIndex(name = "by_activation_ts", def = "{'activationId': 1, 'ts': -1}")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    private Instant ts;                 // เวลาเกิดเหตุการณ์

    private AuditType type;             // ACTIVATE_OK/FAIL, VALIDATE_OK/FAIL, HEARTBEAT, ...

    private String licenseId;           // อ้างถึง licenses._id
    private String activationId;        // อาจเป็น null ในบางเหตุการณ์

    private String deviceFingerprint;   // HWID
    private String ip;                  // ไอพีที่เรียก
    private String userAgent;           // UA หรือ client signature

    private String reason;              // ข้อความอธิบายสาเหตุ error/ban
}
