import Foundation
import Combine

class MessageViewModel: ObservableObject {
    @Published var messages: [MessageModel] = []
    @Published var contactList: [String] = []
    @Published var currentUser: String = ""
    @Published var isConnecting = false
    
    private var socket: NetworkSocket?
    private var messageID = 0
    
    func login(host: String, port: Int, username: String, password: String, completion: @escaping (Bool, String?) -> Void) {
        isConnecting = true
        
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            do {
                self?.socket = try NetworkSocket(host: host, port: port)
                
                // Send login command to server
                // Format: username password 1 (1 = login operation)
                let loginCommand = "\(username) \(password) 1"
                try self?.socket?.send(loginCommand)
                
                // Wait for response
                if let response = try self?.socket?.receive() {
                    DispatchQueue.main.async {
                        self?.isConnecting = false
                        self?.currentUser = username
                        completion(true, nil)
                        self?.startListeningForMessages()
                    }
                } else {
                    throw NetworkError.connectionFailed
                }
            } catch {
                DispatchQueue.main.async {
                    self?.isConnecting = false
                    completion(false, error.localizedDescription)
                }
            }
        }
    }
    
    func sendMessage(to receiver: String, body: String) {
        guard !receiver.isEmpty && !body.isEmpty else { return }
        
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            do {
                self?.messageID += 1
                // Format: sender receiver body operation
                // 3 = send message operation
                let command = "\(self?.currentUser ?? "") \(receiver) \(body) 3"
                try self?.socket?.send(command)
                
                // Add message to local view immediately
                let message = MessageModel(
                    sender: self?.currentUser ?? "",
                    receiver: receiver,
                    body: body,
                    isRead: false,
                    messageID: self?.messageID ?? 0,
                    timestamp: Date()
                )
                
                DispatchQueue.main.async {
                    self?.messages.append(message)
                }
            } catch {
                print("Failed to send message: \(error)")
            }
        }
    }
    
    func loadMessages(for contact: String) {
        messages = []
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            do {
                // Request message history: sender receiver operation(2)
                let command = "\(self?.currentUser ?? "") \(contact) 2"
                try self?.socket?.send(command)
                
                if let response = try self?.socket?.receive() {
                    let messageLines = response.split(separator: "\n").map(String.init)
                    DispatchQueue.main.async {
                        for line in messageLines {
                            let parts = line.split(separator: "|", maxSplits: 3, omittingEmptySubsequences: false).map(String.init)
                            if parts.count >= 4 {
                                let message = MessageModel(
                                    sender: parts[0],
                                    receiver: parts[1],
                                    body: parts[2],
                                    isRead: parts[3] == "true",
                                    messageID: Int.random(in: 0..<10000),
                                    timestamp: Date()
                                )
                                self?.messages.append(message)
                            }
                        }
                    }
                }
            } catch {
                print("Failed to load messages: \(error)")
            }
        }
    }
    
    func loadContacts() {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            do {
                // Request contact list: username operation(4)
                let command = "\(self?.currentUser ?? "") 4"
                try self?.socket?.send(command)
                
                if let response = try self?.socket?.receive() {
                    let contacts = response.split(separator: "\n").map(String.init).filter { !$0.isEmpty }
                    DispatchQueue.main.async {
                        self?.contactList = contacts
                    }
                }
            } catch {
                print("Failed to load contacts: \(error)")
            }
        }
    }
    
    private func startListeningForMessages() {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            while let message = try? self?.socket?.receive() {
                // Parse incoming message
                let parts = message.split(separator: "|", maxSplits: 3, omittingEmptySubsequences: false).map(String.init)
                if parts.count >= 4 {
                    let msg = MessageModel(
                        sender: parts[0],
                        receiver: parts[1],
                        body: parts[2],
                        isRead: parts[3] == "true",
                        messageID: Int.random(in: 0..<10000),
                        timestamp: Date()
                    )
                    
                    DispatchQueue.main.async {
                        self?.messages.append(msg)
                    }
                }
            }
        }
    }
    
    func disconnect() {
        socket?.close()
        messages = []
        contactList = []
        currentUser = ""
    }
}

struct MessageModel: Identifiable {
    let id = UUID()
    let sender: String
    let receiver: String
    let body: String
    let isRead: Bool
    let messageID: Int
    let timestamp: Date
}

// MARK: - Network Socket

class NetworkSocket {
    private var inputStream: InputStream?
    private var outputStream: OutputStream?
    
    init(host: String, port: Int) throws {
        var inputStream: InputStream?
        var outputStream: OutputStream?
        
        Stream.getStreamsToHost(withName: host, port: port, inputStream: &inputStream, outputStream: &outputStream)
        
        guard let input = inputStream, let output = outputStream else {
            throw NetworkError.connectionFailed
        }
        
        self.inputStream = input
        self.outputStream = output
        
        input.schedule(in: .main, forMode: .common)
        output.schedule(in: .main, forMode: .common)
        
        input.open()
        output.open()
    }
    
    func send(_ data: String) throws {
        guard let output = outputStream else { throw NetworkError.connectionFailed }
        
        let bytes = (data + "\n").utf8.map { $0 }
        let written = output.write(bytes, maxLength: bytes.count)
        
        if written < 0 {
            throw NetworkError.sendFailed
        }
    }
    
    func receive() throws -> String? {
        guard let input = inputStream else { throw NetworkError.connectionFailed }
        
        var buffer = [UInt8](repeating: 0, count: 1024)
        let bytesRead = input.read(&buffer, maxLength: buffer.count)
        
        if bytesRead > 0 {
            return String(bytes: buffer[0..<bytesRead], encoding: .utf8)
        }
        
        return nil
    }
    
    func close() {
        inputStream?.close()
        outputStream?.close()
    }
}

enum NetworkError: Error {
    case connectionFailed
    case sendFailed
    case receiveFailed
}
