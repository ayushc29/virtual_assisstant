package Users;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class UserSystem {
    private Map<String, String> users; // Map to store hashed usernames and hashed passwords
    private static final String USERS_FILE = "C:\\Users\\HP\\Desktop\\virtual_assisstant\\src\\main\\java\\Users\\users.txt";

    public UserSystem() {
        users = new HashMap<>();
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                String hashedUsername = parts[0];
                String hashedPassword = parts[1];
                users.put(hashedUsername, hashedPassword);
            }
        } catch (IOException e) {
            System.err.println("Error loading users from file: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
        }
    }

    public void createUser(String username, String password) {
        String hashedUsername = hashString(username);
        String hashedPassword = hashString(password);
        users.put(hashedUsername, hashedPassword);
        saveUsersToFile(); // Save new user to file
    }

    public boolean isUserExists(String username) {
        String hashedUsername = hashString(username);
        return users.containsKey(hashedUsername);
    }

    public boolean authenticate(String username, String password) {
        String hashedUsername = hashString(username);
        String hashedPassword = hashString(password);
        String storedPassword = users.get(hashedUsername);
        return storedPassword != null && storedPassword.equals(hashedPassword);
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing string: " + e.getMessage());
        }
    }
}
