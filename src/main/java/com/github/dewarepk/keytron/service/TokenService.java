package com.github.dewarepk.keytron.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;

@Component
@RequiredArgsConstructor
public class TokenService {

    private final KeyPair keyPair;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.ttlHours}")
    private long ttl;

    public String issue(String licId, String actId, String productCode, String plan) {
        var now = java.time.Instant.now();
        return io.jsonwebtoken.Jwts.builder()
                .setIssuer(issuer)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(now.plus(java.time.Duration.ofHours(ttl))))
                .claim("lic", licId).claim("act", actId).claim("prd", productCode).claim("plan", plan)
                .signWith(keyPair.getPrivate(), io.jsonwebtoken.SignatureAlgorithm.ES256)
                .compact();
    }
}
