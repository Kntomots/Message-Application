# TCP/IP Messaging Application

A Java-based network messaging system that allows multiple clients to communicate with each other through a central server using TCP/IP sockets.

## Overview

**Diktya** is a simple yet functional messaging application built with Java sockets. It enables users to:

- Register/Create accounts
- View a list of all registered users
- Send messages to other users
- Check unread messages
- Read specific messages
- Delete messages

## Project Structure

```
diktya/
├── src/
│   ├── Server.java           # Main server that listens for client connections
│   ├── Client.java           # Client application for user interaction
│   ├── ClientHandler.java    # Handles individual client connections and requests
│   ├── Account.java          # User account model with messages
│   ├── Message.java          # Message model
│   ├── MessagingApp.java     # GUI application entry point (Swing)
│   ├── LoginFrame.java       # Login UI window
│   ├── ChatFrame.java        # Main chat UI window
│   └── ClientConnection.java # Socket connection handler for GUI
├── README.md
└── diktya.iml
```

## Requirements

- **Java Development Kit (JDK) 8** or higher
- **Java Runtime Environment (JRE) 8** or higher for running compiled classes
- **No external dependencies** - Uses only built-in Java Swing library

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Kntomots/diktya.git
cd diktya
```

### 2. Compile the Project

Navigate to the `src` directory and compile all Java files with Java 8 compatibility:

```bash
cd src
javac -source 1.8 -target 1.8 *.java
```

## Running the Application

### Option 1: Using the GUI (Recommended for Users)

#### Step 1: Start the Server

Open a PowerShell/Command Prompt terminal:

```bash
cd src
java Server 5000
```

#### Step 2: Launch the GUI Application

Open a **new** PowerShell/Command Prompt terminal:

```bash
cd src
java MessagingApp
```

A login window will appear. Enter:
- **Host:** `localhost`
- **Port:** `5000`
- **Username:** Your username
- Click **Login**

Features:
- Clean, intuitive interface
- Contact list dropdown
- Real-time messaging
- Message history (shows all messages with selected contact)
- Refresh contacts button

### Option 2: Using Command-Line Client (For Automation/Testing)

Open a **new** PowerShell/Command Prompt terminal and run:

```bash
cd src
java Client localhost 5000 <operation> <arguments>
```

## Client Operations

The client supports the following operations:

### Operation 1: Register/Create Account

**Command:**

```bash
java -cp . Client localhost 5000 1 <username>
```

**Example:**

```bash
java -cp . Client localhost 5000 1 kntomo
```

**Response:** Returns an authentication token (e.g., `6851`)

**Note:** The username must contain only alphanumeric characters and underscores.

---

### Operation 2: List All Users

**Command:**

```bash
java -cp . Client localhost 5000 2 <auth_token>
```

**Example:**

```bash
java -cp . Client localhost 5000 2 6851
```

**Response:** A numbered list of all registered users

---

### Operation 3: Send Message

**Command:**

```bash
java -cp . Client localhost 5000 3 <auth_token> <recipient_username> <message_text>
```

**Example:**

```bash
java -cp . Client localhost 5000 3 6851 john Hello how are you
```

**Response:** `OK` if successful, or error message

---

### Operation 4: Check Unread Messages

**Command:**

```bash
java -cp . Client localhost 5000 4 <auth_token>
```

**Example:**

```bash
java -cp . Client localhost 5000 4 6851
```

**Response:** List of unread messages with sender names (marked with `*`)

---

### Operation 5: Read Specific Message

**Command:**

```bash
java -cp . Client localhost 5000 5 <auth_token> <message_id>
```

**Example:**

```bash
java -cp . Client localhost 5000 5 6851 1
```

**Response:** The message content with sender's name, marks message as read

---

### Operation 6: Delete Message

**Command:**

```bash
java -cp . Client localhost 5000 6 <auth_token> <message_id>
```

**Example:**

```bash
java -cp . Client localhost 5000 6 6851 1
```

**Response:** `OK` if successful, or error message

---

## Example Workflow with GUI

1. **Terminal 1 - Start Server:**

   ```bash
   cd src
   java Server 5000
   ```

2. **Terminal 2 - Launch GUI Application:**

   ```bash
   cd src
   java MessagingApp
   ```

3. **In the Login Window:**
   - Host: `localhost`
   - Port: `5000`
   - Username: `alice`
   - Click **Login**

4. **In the Chat Window:**
   - Click **Refresh** to load contacts
   - Select a contact from the dropdown
   - Type a message in the input field
   - Press **Enter** or click **Send**
   - View message history in the chat area

5. **Multiple Users:**
   - Open another instance: `java MessagingApp` in a new terminal
   - Login with different credentials
   - Both windows will show contacts and can exchange messages in real-time

## Example Workflow with Command-Line Client

1. **Terminal 1 - Start Server:**

   ```bash
   java Server 5000
   ```
2. **Terminal 2 - Register User 1:**

   ```bash
   java Client localhost 5000 1 alice
   # Output: Success (or your auth response)
   ```
3. **Terminal 3 - Register User 2:**your Java installation:

```bash
C:\Program Files\Java\jdk1.8.0_361\bin\javac.exe *.java
```

### Issue: GUI Window Doesn't Appear

**Solution:** Make sure the server is running first. Check that:
1. Server is started in another terminal: `java Server 5000`
2. Port 5000 is not in use
3. Firewall is not blocking the connection

### Issue: "UnsupportedClassVersionError"

**Solution:** Recompile with Java 8 compatibility:

## Troubleshooting

### Issue: "javac is not recognized"

**Solution:** Add Java to your PATH or use the full path to your Java installation:

```bash
C:\Program Files\Java\jdk1.8.0_361\bin\javac.exe *.java
```

### Issue: GUI Window Doesn't Appear

**Solution:** Make sure the server is running first. Check that:
1. Server is started in another terminal: `java Server 5000`
2. Port 5000 is not in use
3. Firewall is not blocking the connection

### Issue: "UnsupportedClassVersionError"

**Solution:** Recompile with Java 8 compatibility:

```bash
javac -source 1.8 -target 1.8 *.java
```

### Issue: "Connection refused"

**Solution:** 
1. Make sure the server is running: `java Server 5000`
2. Verify port number matches (should be 5000)
3. Check host address (use `localhost` for local testing)

## Architecture

- **Server.java**: Listens for incoming client connections and spawns a new thread for each client
- **ClientHandler.java**: Processes client requests and manages the protocol logic
- **Account.java**: Stores user account data (username, auth token, messages)
- **Message.java**: Represents a single message with sender, receiver, body, and read status
- **Client.java**: Command-line user interface for testing
- **MessagingApp.java**: Swing GUI application entry point
- **LoginFrame.java**: Swing login window UI
- **ChatFrame.java**: Swing chat window UI
- **ClientConnection.java**: Socket communication handler for the GUI

## Notes

- Messages are stored in memory only (no database persistence)
- The server runs indefinitely until terminated
- Multiple clients (GUI and command-line) can connect and interact simultaneously
- GUI provides real-time messaging with contact list and message history
- Command-line client is useful for scripting and automation testing
- All authentication tokens are randomly generated 4-digit numbers (1000-9999)

## Author

Kntomots

## License

Open source - Feel free to modify and use as needed
