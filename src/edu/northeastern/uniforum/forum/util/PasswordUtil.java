// com.uniforum.utils.PasswordUtil.java

package edu.northeastern.uniforum.forum.util;

import org.mindrot.jbcrypt.BCrypt; 

public class PasswordUtil {

    /**
     * Hashes a plaintext password using the BCrypt algorithm.
     * @param plaintextPassword The password provided by the user during registration.
     * @return The securely hashed password string.
     */
    public static String hashPassword(String plaintextPassword) {
        // Generates a random salt and hashes the password 
        return BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
    }

    /**
     * Checks if a plaintext password (entered during login) matches a stored hash.
     * @param plaintextPassword The password provided during login.
     * @param storedHash The hash retrieved from the database.
     * @return True if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plaintextPassword, String storedHash) {
        // BCrypt automatically handles comparing the plaintext password with the stored hash
        // (which contains the salt).
        return BCrypt.checkpw(plaintextPassword, storedHash);
    }
}