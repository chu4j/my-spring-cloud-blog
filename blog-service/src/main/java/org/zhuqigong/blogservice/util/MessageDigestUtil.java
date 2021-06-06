package org.zhuqigong.blogservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MessageDigestUtil.class);

    private MessageDigestUtil() {
    }

    public static String sha2(String text) {
        String algorithmName = "SHA-256";
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithmName);
            byte[] encrypt = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return new String(encrypt);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("No  NoSuchAlgorithm of {}", algorithmName);
            return null;
        }
    }
}
