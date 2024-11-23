package nintaco.util;

import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import static nintaco.util.CollectionsUtil.*;

public final class PasswordUtil {

    private static final int ITERATIONS = 16411;
    private static final int SALT_LENGTH = 32;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
    }

    public static byte[] createSalt() {
        try {
            return SecureRandom.getInstance("SHA1PRNG").generateSeed(SALT_LENGTH);
        } catch (final Throwable t) {
            return null;
        }
    }

    public static byte[] createHash(final char[] password,
                                    final byte[] salt) {
        try {
            if (isBlank(password) || isBlank(salt)) {
                return null;
            }
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(
                    new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)).getEncoded();
        } catch (final Throwable t) {
            return null;
        }
    }

    public static boolean compare(final char[] enteredPassword,
                                  final byte[] hash, final byte[] salt) {
        try {
            if (isBlank(enteredPassword) || isBlank(hash) || isBlank(salt)) {
                return false;
            }
            return compareArrays(hash, createHash(enteredPassword, salt));
        } catch (final Throwable t) {
            return false;
        }
    }
}
