package com.github.dewarepk.keytron.config;

import com.github.dewarepk.keytron.model.LicenseStatus;
import com.github.dewarepk.keytron.model.Product;
import com.github.dewarepk.keytron.repo.ProductRepo;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;

//@Configuration
public class DataInitializer {

    //@Bean
    ApplicationRunner initDb(ProductRepo products, MongoTemplate template) {
        return args -> {
            if (!template.collectionExists(Product.class)) {
                template.createCollection(Product.class); // บังคับให้ DB ถูกสร้าง
            }
            products.findByCode("PRO").orElseGet(() ->
                    products.save(Product.builder()
                            .code("PRO").name("MyApp Pro").status(LicenseStatus.ACTIVE)
                            .createdAt(Instant.now()).build())
            );
        };
    }
}
