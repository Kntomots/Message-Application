import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LoginUI {
    private Stage stage;
    private ClientConnection clientConnection;
    
    public LoginUI(Stage stage) {
        this.stage = stage;
    }
    
    public void show() {
        stage.setTitle("Message Application");
        stage.setWidth(450);
        stage.setHeight(550);
        
        VBox root = new VBox();
        root.setPadding(new Insets(30));
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Message App");
        titleLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        Label subtitleLabel = new Label("Connect & Chat");
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");
        
        // Server Configuration Section
        VBox configSection = createSection("Server Configuration");
        HBox hostBox = new HBox(10);
        Label hostLabel = new Label("Host:");
        hostLabel.setPrefWidth(70);
        TextField hostField = new TextField("localhost");
        hostBox.getChildren().addAll(hostLabel, hostField);
        hostBox.setStyle("-fx-padding: 5;");
        
        HBox portBox = new HBox(10);
        Label portLabel = new Label("Port:");
        portLabel.setPrefWidth(70);
        TextField portField = new TextField("5000");
        portField.setPrefWidth(150);
        portBox.getChildren().addAll(portLabel, portField);
        portBox.setStyle("-fx-padding: 5;");
        
        configSection.getChildren().addAll(hostBox, portBox);
        
        // Login Credentials Section
        VBox credSection = createSection("Login Credentials");
        
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-weight: bold;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        
        credSection.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField);
        
        // Button and Status
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #2196F3;");
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #d32f2f;");
        
        // Login action
        loginButton.setOnAction(e -> {
            String host = hostField.getText().trim();
            String portStr = portField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password");
                return;
            }
            
            if (host.isEmpty() || portStr.isEmpty()) {
                statusLabel.setText("Please enter host and port");
                return;
            }
            
            loginButton.setDisable(true);
            statusLabel.setText("Connecting...");
            
            try {
                int port = Integer.parseInt(portStr);
                clientConnection = new ClientConnection(host, port);
                
                // Send login command: username password 1
                String loginCommand = username + " " + password + " 1";
                clientConnection.sendMessage(loginCommand);
                
                String response = clientConnection.receiveMessage();
                
                if (response != null && !response.isEmpty()) {
                    // Login successful
                    ChatUI chatUI = new ChatUI(clientConnection, username);
                    chatUI.show(stage);
                } else {
                    statusLabel.setText("Login failed: No response from server");
                    loginButton.setDisable(false);
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid port number");
                loginButton.setDisable(false);
            } catch (Exception ex) {
                statusLabel.setText("Connection failed: " + ex.getMessage());
                loginButton.setDisable(false);
            }
        });
        
        root.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Separator(),
            configSection,
            credSection,
            loginButton,
            statusLabel
        );
        
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        
        Scene scene = new Scene(scrollPane);
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createSection(String title) {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: white;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        section.getChildren().add(titleLabel);
        
        return section;
    }
}
