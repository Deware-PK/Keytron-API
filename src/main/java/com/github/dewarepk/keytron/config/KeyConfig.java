package com.github.dewarepk.keytron.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class KeyConfig {

    @Value("${app.jwt.privateKeyLocation}")
    private Resource privateKeyRes;

    @Value("${app.jwt.publicKeyLocation}")
    private Resource publicKeyRes;

    @Bean
    public KeyPair keyPair() throws Exception {
        PrivateKey priv = loadPrivateKey(privateKeyRes, "EC"); // ใช้ "RSA" ถ้าเป็น RSA
        PublicKey pub  = loadPublicKey(publicKeyRes, "EC");
        return new KeyPair(pub, priv);
    }

    private PrivateKey loadPrivateKey(Resource res, String alg) throws Exception {
        String pem = new String(res.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        String base64 = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return java.security.KeyFactory.getInstance(alg)
                .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(base64)));
    }

    private PublicKey loadPublicKey(Resource res, String alg) throws Exception {
        String pem = new String(res.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        String base64 = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return java.security.KeyFactory.getInstance(alg)
                .generatePublic(new java.security.spec.X509EncodedKeySpec(java.util.Base64.getDecoder().decode(base64)));
    }
}
