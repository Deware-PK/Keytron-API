package com.github.dewarepk.keytron.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document("products")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String name;

    @Builder.Default
    private LicenseStatus status = LicenseStatus.ACTIVE;

    @CreatedDate
    private Instant createdAt;

}
