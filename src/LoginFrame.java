import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JLabel statusLabel;
    private JButton loginButton;
    
    public LoginFrame() {
        setTitle("Message Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Title
        JLabel titleLabel = new JLabel("Message App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(33, 150, 243));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Connect & Chat");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(153, 153, 153));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Server Configuration Section
        JPanel configPanel = createSection("Server Configuration");
        
        JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hostPanel.setBackground(Color.WHITE);
        JLabel hostLabel = new JLabel("Host:");
        hostLabel.setPreferredSize(new Dimension(70, 25));
        hostField = new JTextField("localhost", 20);
        hostPanel.add(hostLabel);
        hostPanel.add(hostField);
        
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portPanel.setBackground(Color.WHITE);
        JLabel portLabel = new JLabel("Port:");
        portLabel.setPreferredSize(new Dimension(70, 25));
        portField = new JTextField("5000", 20);
        portPanel.add(portLabel);
        portPanel.add(portField);
        
        configPanel.add(hostPanel);
        configPanel.add(portPanel);
        
        // Login Credentials Section
        JPanel credPanel = createSection("Username");
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        usernameField = new JTextField(25);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        credPanel.add(usernameLabel);
        credPanel.add(usernameField);
        
        // Login Button
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setBackground(new Color(33, 150, 243));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> handleLogin());
        
        // Status Label
        statusLabel = new JLabel();
        statusLabel.setForeground(new Color(211, 47, 47));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add all components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(configPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(credPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalGlue());
        
        add(mainPanel);
    }
    
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        
        return panel;
    }
    
    private void handleLogin() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            statusLabel.setText("Please enter username");
            return;
        }
        
        if (host.isEmpty() || portStr.isEmpty()) {
            statusLabel.setText("Please enter host and port");
            return;
        }
        
        loginButton.setEnabled(false);
        statusLabel.setText("Connecting...");
        
        new Thread(() -> {
            try {
                int port = Integer.parseInt(portStr);
                ClientConnection clientConnection = new ClientConnection(host, port);
                
                // Send login command: host port 1 username
                String loginCommand = host + " " + port + " 1 " + username;
                clientConnection.sendMessage(loginCommand);
                
                String response = clientConnection.receiveMessage();
                
                if (response != null && !response.isEmpty() && !response.contains("Invalid") && !response.contains("Sorry")) {
                    // Login successful - response is auth token
                    String authToken = response.trim();
                    SwingUtilities.invokeLater(() -> {
                        ChatFrame chatFrame = new ChatFrame(clientConnection, username, authToken);
                        chatFrame.setVisible(true);
                        LoginFrame.this.dispose();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Login failed: " + (response != null ? response : "No response"));
                        loginButton.setEnabled(true);
                    });
                }
            } catch (NumberFormatException ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Invalid port number");
                    loginButton.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connection failed: " + ex.getMessage());
                    loginButton.setEnabled(true);
                });
            }
        }).start();
    }
}
