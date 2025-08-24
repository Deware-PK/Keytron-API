package com.github.dewarepk.keytron.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Component
public class LicenseKeyUtil {


    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RNG = new SecureRandom();

    @Value("${app.secret:change-me}")
    private String hmacSecret;

    public String generateKey(String prefix, int groups, int groupLen) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null && !prefix.isBlank()) {
            sb.append(prefix.toUpperCase()).append('-');
        }
        for (int g = 0; g < groups; g++) {
            if (g > 0) sb.append('-');
            for (int i = 0; i < groupLen; i++) {
                sb.append(ALPHABET[RNG.nextInt(ALPHABET.length)]);
            }
        }
        return sb.toString();
    }

    public String generateKey(String prefix) {
        return generateKey(prefix, 4, 4);
    }

    public String normalize(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("License key is empty/null");
        }
        return rawKey.replaceAll("[-\\s]", "").toUpperCase();
    }




    private byte[] decodeSecret(String s) {
        if (s.startsWith("hex:")) {
            return java.util.HexFormat.of().parseHex(s.substring(4));
        }
        if (s.startsWith("base64:")) {
            return java.util.Base64.getDecoder().decode(s.substring(7));
        }
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public String hashKey(String rawKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(decodeSecret(hmacSecret), "HmacSHA256"));
            byte[] h = mac.doFinal(normalize(rawKey).getBytes(StandardCharsets.UTF_8));
            return toHex(h);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot HMAC license key", e);
        }
    }


    /** ตรวจรูปแบบคีย์เบื้องต้น (กลุ่ม A-Z2-9 ความยาวตามที่กำหนด) */
    public boolean matchesFormat(String key, int groups, int groupLen) {
        String regex = "^[A-Z]+(-[A-Z2-9]{" + groupLen + "}){" + groups + "}$";
        return key != null && key.toUpperCase().matches(regex);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >>> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}
