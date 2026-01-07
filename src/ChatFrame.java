import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatFrame extends JFrame {
    private ClientConnection clientConnection;
    private String currentUser;
    private String authToken;
    private String selectedContact = "";
    
    private JTextArea messageArea;
    private JTextField messageInput;
    private JComboBox<String> contactList;
    private JLabel userInfoLabel;
    
    public ChatFrame(ClientConnection clientConnection, String currentUser, String authToken) {
        this.clientConnection = clientConnection;
        this.currentUser = currentUser;
        this.authToken = authToken;
        
        setTitle("Messages - " + currentUser);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);
        
        // Separator
        mainPanel.add(new JSeparator(), BorderLayout.NORTH);
        
        // Center content
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Sidebar - Contact list
        JPanel sidebar = createSidebar();
        centerPanel.add(sidebar, BorderLayout.WEST);
        
        // Chat area
        JPanel chatArea = createChatArea();
        centerPanel.add(chatArea, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Load contacts
        loadContacts();
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(Color.WHITE);
        
        JLabel messageLabel = new JLabel("Messages");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        userInfoLabel = new JLabel("User: " + currentUser);
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        userInfoLabel.setForeground(new Color(153, 153, 153));
        
        userInfo.add(messageLabel);
        userInfo.add(userInfoLabel);
        
        header.add(userInfo, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 12));
        logoutButton.addActionListener(e -> logout());
        header.add(logoutButton, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel contactsLabel = new JLabel("Contacts");
        contactsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        contactList = new JComboBox<>();
        contactList.addActionListener(e -> {
            selectedContact = (String) contactList.getSelectedItem();
            if (selectedContact != null && !selectedContact.isEmpty()) {
                loadMessages();
            }
        });
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadContacts());
        
        sidebar.add(contactsLabel, BorderLayout.NORTH);
        sidebar.add(contactList, BorderLayout.CENTER);
        sidebar.add(refreshButton, BorderLayout.SOUTH);
        
        return sidebar;
    }
    
    private JPanel createChatArea() {
        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel chatLabel = new JLabel("Chat");
        chatLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        messageArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        messageInput = new JTextField();
        messageInput.setFont(new Font("Arial", Font.PLAIN, 12));
        messageInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        chatArea.add(chatLabel, BorderLayout.NORTH);
        chatArea.add(scrollPane, BorderLayout.CENTER);
        chatArea.add(inputPanel, BorderLayout.SOUTH);
        
        return chatArea;
    }
    
    private void loadContacts() {
        new Thread(() -> {
            try {
                String command = "localhost 5000 2 " + authToken;
                System.out.println("Sending: " + command);
                clientConnection.sendMessage(command);
                
                String response = clientConnection.receiveMessage();
                System.out.println("Response: " + response);
                
                if (response != null && !response.isEmpty()) {
                    String[] contacts = response.split("/n");
                    SwingUtilities.invokeLater(() -> {
                        contactList.removeAllItems();
                        for (String contact : contacts) {
                            String trimmed = contact.trim();
                            if (!trimmed.isEmpty() && trimmed.contains(". ")) {
                                String username = trimmed.split(". ", 2)[1];
                                System.out.println("Adding contact: " + username);
                                contactList.addItem(username);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading contacts: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private void loadMessages() {
        messageArea.setText("Loading messages from " + selectedContact + "...");
        new Thread(() -> {
            try {
                String command = "localhost 5000 4 " + authToken;
                System.out.println("Sending: " + command);
                clientConnection.sendMessage(command);
                
                String response = clientConnection.receiveMessage();
                System.out.println("Response: " + response);
                
                if (response != null && !response.isEmpty()) {
                    String[] messages = response.split("/n");
                    StringBuilder sb = new StringBuilder();
                    
                    for (String msg : messages) {
                        if (!msg.trim().isEmpty()) {
                            System.out.println("Message: " + msg);
                            sb.append(msg).append("\n");
                        }
                    }
                    
                    final String messageText = sb.toString();
                    SwingUtilities.invokeLater(() -> {
                        if (messageText.isEmpty()) {
                            messageArea.setText("No messages with " + selectedContact);
                        } else {
                            messageArea.setText(messageText);
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> messageArea.setText("No messages"));
                }
            } catch (Exception e) {
                System.err.println("Error loading messages: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> messageArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
    
    private void sendMessage() {
        String message = messageInput.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (selectedContact == null || selectedContact.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select a contact before sending a message.",
                "No Contact Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        messageInput.setEnabled(false);
        
        new Thread(() -> {
            try {
                String command = "localhost 5000 3 " + authToken + " " + selectedContact + " " + message;
                System.out.println("Sending: " + command);
                clientConnection.sendMessage(command);
                
                String response = clientConnection.receiveMessage();
                System.out.println("Response: " + response);
                
                SwingUtilities.invokeLater(() -> {
                    messageInput.setEnabled(true);
                    if (response != null && response.contains("OK")) {
                        messageArea.append("[You]: " + message + "\n");
                        messageInput.setText("");
                    } else {
                        JOptionPane.showMessageDialog(ChatFrame.this,
                            "Failed to send message: " + response,
                            "Send Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    messageInput.setEnabled(true);
                    JOptionPane.showMessageDialog(ChatFrame.this,
                        "Error: " + e.getMessage(),
                        "Send Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void logout() {
        try {
            clientConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            ChatFrame.this.dispose();
        });
    }
}
