import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatUI {
    private Stage stage;
    private ClientConnection clientConnection;
    private String currentUser;
    private String selectedContact = "";
    
    private TextArea messageArea;
    private TextField messageInput;
    private ComboBox<String> contactList;
    private Label userInfoLabel;
    
    public ChatUI(ClientConnection clientConnection, String currentUser) {
        this.clientConnection = clientConnection;
        this.currentUser = currentUser;
    }
    
    public void show(Stage stage) {
        this.stage = stage;
        stage.setTitle("Messages - " + currentUser);
        stage.setWidth(800);
        stage.setHeight(600);
        
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        HBox header = createHeader();
        
        // Main content area
        HBox mainContent = new HBox();
        mainContent.setSpacing(10);
        
        // Sidebar - Contact list
        VBox sidebar = createSidebar();
        
        // Chat area
        VBox chatArea = createChatArea();
        
        mainContent.getChildren().addAll(sidebar, chatArea);
        HBox.setHgrow(chatArea, Priority.ALWAYS);
        
        root.getChildren().addAll(header, new Separator(), mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        // Load contacts
        loadContacts();
        
        // Start listening for incoming messages
        startMessageListener();
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setSpacing(15);
        header.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        VBox userInfo = new VBox();
        userInfo.setSpacing(5);
        
        Label messageLabel = new Label("Messages");
        messageLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        userInfoLabel = new Label("User: " + currentUser);
        userInfoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");
        
        userInfo.getChildren().addAll(messageLabel, userInfoLabel);
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-padding: 5 15; -fx-font-size: 12;");
        logoutButton.setOnAction(e -> {
            try {
                clientConnection.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            LoginUI loginUI = new LoginUI(stage);
            loginUI.show();
        });
        
        header.getChildren().addAll(userInfo, new Separator());
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        header.getChildren().add(logoutButton);
        
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(180);
        sidebar.setSpacing(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        Label contactsLabel = new Label("Contacts");
        contactsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        
        contactList = new ComboBox<>();
        contactList.setPrefWidth(Double.MAX_VALUE);
        contactList.setOnAction(e -> {
            selectedContact = contactList.getValue();
            if (selectedContact != null && !selectedContact.isEmpty()) {
                loadMessages();
            }
        });
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setPrefWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(e -> loadContacts());
        
        sidebar.getChildren().addAll(contactsLabel, contactList, refreshButton);
        
        return sidebar;
    }
    
    private VBox createChatArea() {
        VBox chatArea = new VBox();
        chatArea.setSpacing(10);
        chatArea.setPadding(new Insets(10));
        chatArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        // Message display area
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-control-inner-background: #fafafa; -fx-font-family: 'Courier New';");
        
        // Input area
        HBox inputBox = new HBox();
        inputBox.setSpacing(10);
        
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                sendMessage();
            }
        });
        
        Button sendButton = new Button("Send");
        sendButton.setPrefWidth(80);
        sendButton.setOnAction(e -> sendMessage());
        
        inputBox.getChildren().addAll(messageInput, sendButton);
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        
        chatArea.getChildren().addAll(
            new Label("Chat"),
            messageArea,
            inputBox
        );
        VBox.setVgrow(messageArea, Priority.ALWAYS);
        
        return chatArea;
    }
    
    private void loadContacts() {
        new Thread(() -> {
            try {
                // Request contact list: username 4
                String command = currentUser + " 4";
                clientConnection.sendMessage(command);
                
                String response = clientConnection.receiveMessage();
                if (response != null && !response.isEmpty()) {
                    String[] contacts = response.split("\n");
                    Platform.runLater(() -> {
                        contactList.getItems().clear();
                        for (String contact : contacts) {
                            if (!contact.trim().isEmpty()) {
                                contactList.getItems().add(contact.trim());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void loadMessages() {
        messageArea.clear();
        new Thread(() -> {
            try {
                // Request messages: username receiver 2
                String command = currentUser + " " + selectedContact + " 2";
                clientConnection.sendMessage(command);
                
                String response = clientConnection.receiveMessage();
                if (response != null && !response.isEmpty()) {
                    String[] messages = response.split("\n");
                    StringBuilder sb = new StringBuilder();
                    
                    for (String msg : messages) {
                        if (!msg.trim().isEmpty()) {
                            String[] parts = msg.split("\\|");
                            if (parts.length >= 3) {
                                String sender = parts[0].trim();
                                String body = parts[2].trim();
                                String prefix = sender.equals(currentUser) ? "[You]" : "[" + sender + "]";
                                sb.append(prefix).append(": ").append(body).append("\n");
                            }
                        }
                    }
                    
                    Platform.runLater(() -> messageArea.setText(sb.toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void sendMessage() {
        String message = messageInput.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (selectedContact.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText("Please select a contact");
            alert.setContentText("You must select a contact before sending a message.");
            alert.showAndWait();
            return;
        }
        
        new Thread(() -> {
            try {
                // Send message: sender receiver body 3
                String command = currentUser + " " + selectedContact + " " + message + " 3";
                clientConnection.sendMessage(command);
                
                String response = clientConnection.receiveMessage();
                
                Platform.runLater(() -> {
                    messageArea.appendText("[You]: " + message + "\n");
                    messageInput.clear();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void startMessageListener() {
        new Thread(() -> {
            while (true) {
                try {
                    String message = clientConnection.receiveMessage();
                    if (message != null && !message.isEmpty()) {
                        final String msg = message;
                        Platform.runLater(() -> {
                            if (!selectedContact.isEmpty()) {
                                messageArea.appendText(msg + "\n");
                            }
                        });
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    break;
                }
            }
        }).setDaemon(true);
    }
}
