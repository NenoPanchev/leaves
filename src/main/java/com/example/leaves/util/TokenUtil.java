package com.example.leaves.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class TokenUtil {
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public static String getTokenUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getTokenBytes() {
        byte[] randomBytes = new byte[6];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
