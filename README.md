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
│   └── Message.java          # Message model
├── README.md
└── diktya.iml
```

## Requirements

- **Java Development Kit (JDK) 11** or higher
- **Java Runtime Environment (JRE)** for running compiled classes

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Kntomots/diktya.git
cd diktya
```

### 2. Compile the Project

Navigate to the `src` directory and compile all Java files:

```bash
cd src
javac *.java
```

Or if Java 11 is not in your PATH:

```bash
C:\Program Files\Java\jdk-11\bin\javac.exe *.java
```

## Running the Application

### Step 1: Start the Server

Open a PowerShell/Command Prompt terminal and run:

```bash
cd src
java -cp . Server 5000
```

Or with full Java 11 path:

```bash
& 'C:\Program Files\Java\jdk-11\bin\java.exe' -cp . Server 5000
```

The server will start listening on **port 5000**. You should see no output if it starts successfully.

### Step 2: Start the Client

Open a **new** PowerShell/Command Prompt terminal and run:

```bash
cd src
java -cp . Client localhost 5000 <operation> <arguments>
```

Or with full Java 11 path:

```bash
& 'C:\Program Files\Java\jdk-11\bin\java.exe' -cp . Client localhost 5000 <operation> <arguments>
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

## Example Workflow

1. **Terminal 1 - Start Server:**

   ```bash
   java -cp . Server 5000
   ```
2. **Terminal 2 - Register User 1:**

   ```bash
   java -cp . Client localhost 5000 1 alice
   # Output: 1234 (your auth token)
   ```
3. **Terminal 3 - Register User 2:**

   ```bash
   java -cp . Client localhost 5000 1 bob
   # Output: 5678 (your auth token)
   ```
4. **Terminal 2 - List all users (as alice):**

   ```bash
   java -cp . Client localhost 5000 2 1234
   # Output: 1. alice, 2. bob
   ```
5. **Terminal 3 - Send message to alice (as bob):**

   ```bash
   java -cp . Client localhost 5000 3 5678 alice Hi alice how are you
   # Output: OK
   ```
6. **Terminal 2 - Check unread messages (as alice):**

   ```bash
   java -cp . Client localhost 5000 4 1234
   # Output: 1. from: bob*
   ```
7. **Terminal 2 - Read message 1 (as alice):**

   ```bash
   java -cp . Client localhost 5000 5 1234 1
   # Output: (bob)Hi alice how are you
   ```

## Troubleshooting

### Issue: "javac is not recognized"

**Solution:** Add Java to your PATH or use the full path to Java 11:

```bash
C:\Program Files\Java\jdk-11\bin\javac.exe *.java
```

### Issue: "Could not find or load main class Server"

**Solution:** Make sure you've compiled the files and are in the `src` directory with the `-cp .` flag

### Issue: "UnsupportedClassVersionError"

**Solution:** Use Java 11 to run the application:

```bash
& 'C:\Program Files\Java\jdk-11\bin\java.exe' -cp . Server 5000
```

### Issue: "Connection refused"

**Solution:** Make sure the server is running in another terminal before starting the client

## Architecture

- **Server.java**: Listens for incoming client connections and spawns a new thread for each client
- **ClientHandler.java**: Processes client requests and manages the protocol logic
- **Account.java**: Stores user account data (username, auth token, messages)
- **Message.java**: Represents a single message with sender, receiver, body, and read status
- **Client.java**: User-facing application that connects to the server

## Notes

- All authentication tokens are randomly generated 4-digit numbers (1000-9999)
- Messages are stored in memory only (no database persistence)
- The server runs indefinitely until terminated
- Multiple clients can connect and interact simultaneously

## Author

Kntomots

## License

Open source - Feel free to modify and use as needed
