package me.t65.reportgenapi.utils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NormalizeLinks {

    public static String normalizeLink(String link) {
        return link.toLowerCase().replaceAll("http://", "").replaceAll("https://", "");
    }

    public static long normalizeAndHashLink(String link) {
        // Normalize link - convert to lowercase, remove basic parts
        String normalizedLink = normalizeLink(link);

        // Hash the normalized link
        long hashedLink = hashString(normalizedLink);
        return hashedLink;
    }

    private static long hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            ByteBuffer buffer = ByteBuffer.wrap(hash);
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
