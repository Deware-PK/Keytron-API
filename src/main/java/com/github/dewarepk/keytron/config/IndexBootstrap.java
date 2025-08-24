package com.github.dewarepk.keytron.config;

import com.github.dewarepk.keytron.model.Activation;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.time.Duration;

public class IndexBootstrap {

    @Bean
    ApplicationRunner ensureTtl(MongoTemplate template) {
        return args -> template.indexOps(Activation.class)
                .createIndex(new Index().on("expiresAt", Sort.Direction.ASC).expire(Duration.ZERO));
    }
}
