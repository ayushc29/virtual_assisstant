import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import Users.UserSystem;
import org.json.*;

public class Chat {
    private static JFrame frame;
    private static JPanel panel;
    private static JTextField inputField;
    private static JButton submitButton;
    private static JTextArea outputArea;
    private static UserSystem userSystem;
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private static JButton loginButton;
    private static JButton signupButton;

    public static void main(String[] args) {
        userSystem = new UserSystem();
        frame = new JFrame("VirtualAssistant");

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Virtual Assistant");
        titleLabel.setFont(new Font("Aptos", Font.BOLD, 20));
        loginPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("Username:");
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx++;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx++;
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30)); // Custom button size
        loginButton.setBackground(new Color(50, 150, 250)); // Custom button color
        loginButton.setForeground(Color.WHITE); // Custom text color
        loginButton.setFocusPainted(false); // Remove focus border
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        loginPanel.add(loginButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel signupLabel = new JLabel("Don't have an account?");
        loginPanel.add(signupLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        signupButton = new JButton("Sign Up");
        signupButton.setPreferredSize(new Dimension(100, 30));
        signupButton.setBackground(new Color(50, 150, 250));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        signupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                signup();
            }
        });
        loginPanel.add(signupButton, gbc);

        frame.add(loginPanel);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void signup() {
        JTextField newUsernameField = new JTextField(15);
        JPasswordField newPasswordField = new JPasswordField(15);

        JPanel signupPanel = new JPanel(new GridLayout(3, 1));
        signupPanel.add(new JLabel("Enter a new username:"));
        signupPanel.add(newUsernameField);
        signupPanel.add(new JLabel("Enter a new password:"));
        signupPanel.add(newPasswordField);

        int result = JOptionPane.showConfirmDialog(frame, signupPanel, "Sign Up", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newUsername = newUsernameField.getText();
            String newPassword = new String(newPasswordField.getPassword());

            // Check if the username is available
            if (userSystem.isUserExists(newUsername)) {
                JOptionPane.showMessageDialog(frame, "Username already exists. Please choose a different username.");
            } else {
                // Create a new user account
                userSystem.createUser(newUsername, newPassword);
                JOptionPane.showMessageDialog(frame, "Account created successfully. You can now log in with your new credentials.");
            }
        }
    }


    private static void sendMessage() {
        String input = inputField.getText();
        if (!input.isEmpty()) {
            String response = chatGPT(input);
            response = response.replace("\\n", "\n");
            response = response.replace("\\\"", "\"");
            outputArea.append("User: " + input + "\n");
            outputArea.append("ChatGPT: " + response + "\n\n");
            inputField.setText("");
            // Resize the JTextArea
            outputArea.setPreferredSize(new Dimension(outputArea.getWidth(), outputArea.getPreferredSize().height + outputArea.getFont().getSize() * 2));
        }
    }


    private static String chatGPT(String message) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "YOUR-API-KEY";
        String model = "gpt-3.5-turbo";

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            // Build the request body
            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Get the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            return extractContentFromResponse(response.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (userSystem.authenticate(username, password)) {
            // Successful login
            JOptionPane.showMessageDialog(frame, "Login successful!");
            // Hide login panel
            if (panel != null) {
                Container contentPane = frame.getContentPane();
                contentPane.remove(panel);
                contentPane.revalidate();
                contentPane.repaint();
            }
            // Initialize and display the chat panel
            initializeChatPanel();
        } else {
            // Failed login
            JOptionPane.showMessageDialog(frame, "Invalid username or password. Please try again.");
        }
    }

    private static void initializeChatPanel() {
        frame = new JFrame("VirtualAssistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null); // Center the frame on screen

        panel = new JPanel(new BorderLayout());

        inputField = new JTextField();
        inputField.setPreferredSize(new Dimension(300, 30));

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        submitButton = new JButton("Send");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }




    private static String extractContentFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            String content = firstChoice.getJSONObject("message").getString("content");
            return content;
        } catch (JSONException e) {
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }
}
