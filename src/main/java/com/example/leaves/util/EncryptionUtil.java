package com.example.leaves.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class EncryptionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionUtil.class);
    /* Private variable declaration */
    private static final String SECRET_KEY = "1234!56?78a9";
    private static final String SALTVALUE = "a!b&c/?d*ef!g";

    private EncryptionUtil() {
        throw new IllegalStateException("Util class");
    }

    /* Encryption Method */
    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt != null) {
            try {
                /* Declare a byte array. */
                byte[] iv = {0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0};
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                /* Create factory for secret keys. */
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                /* PBEKeySpec class implements KeySpec interface. */
                KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
                SecretKey tmp = factory.generateSecret(spec);
                SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
                /* Retruns encrypted value. */
                return Base64.getEncoder()
                        .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException |
                     InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException |
                     NoSuchPaddingException e) {
                LOGGER.warn(String.format("Error occured during decryption: %s", e));
            }
        }
        return null;
    }

    /* Decryption Method */
    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt != null) {
            try {
                /* Declare a byte array. */
                byte[] iv = {0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0};
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                /* Create factory for secret keys. */
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                /* PBEKeySpec class implements KeySpec interface. */
                KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
                SecretKey tmp = factory.generateSecret(spec);
                SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
                /* Retruns decrypted value. */
                return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException |
                     InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException |
                     NoSuchPaddingException e) {
                LOGGER.warn(String.format("Error occured during decryption: %s", e));
            }
        }
        return null;
    }

}
