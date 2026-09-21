package chaindb.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides SHA-256 hashing used by the blockchain for block hashes and
 * proof-of-work mining. This is the single point in the application
 * that touches {@link MessageDigest}.
 */
public final class HashUtil {

    private HashUtil() {
        // static utility class
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be present on every standard JVM.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
