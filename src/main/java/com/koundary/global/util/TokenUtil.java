package com.koundary.global.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtil {
    private static final SecureRandom RNG = new SecureRandom();
    private TokenUtil() {}

    public static String generate(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
    public static String generate() {return generate(32);}

    public static String sha256Base64(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().encodeToString(hash);
        }catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
