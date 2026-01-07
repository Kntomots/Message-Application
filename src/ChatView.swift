import SwiftUI

struct ChatView: View {
    @ObservedObject var viewModel: MessageViewModel
    @Binding var isLoggedIn: Bool
    @State private var selectedContact: String = ""
    @State private var messageText = ""
    @State private var showUserList = false
    
    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Messages")
                            .font(.title2)
                            .fontWeight(.bold)
                        Text("User: \(viewModel.currentUser)")
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                    
                    Spacer()
                    
                    HStack(spacing: 12) {
                        Button(action: { showUserList = true }) {
                            Image(systemName: "person.2.fill")
                                .font(.headline)
                        }
                        
                        Button(action: { logout() }) {
                            Image(systemName: "power")
                                .font(.headline)
                        }
                    }
                    .foregroundColor(.blue)
                }
                .padding()
                .background(Color(.systemGray6))
                
                if selectedContact.isEmpty {
                    // No contact selected
                    VStack(spacing: 20) {
                        Image(systemName: "bubble.left.and.bubble.right")
                            .font(.system(size: 60))
                            .foregroundColor(.gray)
                        
                        Text("Select a Contact")
                            .font(.headline)
                            .foregroundColor(.gray)
                        
                        Text("Choose a contact from the list to start messaging")
                            .font(.caption)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                        
                        Button(action: { showUserList = true }) {
                            Text("View Contacts")
                                .fontWeight(.semibold)
                                .foregroundColor(.white)
                                .padding(10)
                                .background(Color.blue)
                                .cornerRadius(8)
                        }
                    }
                    .frame(maxHeight: .infinity)
                } else {
                    // Chat messages
                    ScrollViewReader { proxy in
                        ScrollView {
                            VStack(spacing: 12) {
                                ForEach(viewModel.messages, id: \.messageID) { message in
                                    MessageBubble(message: message, isCurrentUser: message.sender == viewModel.currentUser)
                                }
                            }
                            .padding()
                            .onChange(of: viewModel.messages.count) { _ in
                                proxy.scrollTo(viewModel.messages.last?.messageID, anchor: .bottom)
                            }
                        }
                    }
                    
                    // Message input
                    VStack(spacing: 12) {
                        HStack(spacing: 12) {
                            TextField("Type a message...", text: $messageText)
                                .padding(10)
                                .background(Color(.systemGray6))
                                .cornerRadius(8)
                            
                            Button(action: sendMessage) {
                                Image(systemName: "paperplane.fill")
                                    .foregroundColor(.blue)
                                    .font(.headline)
                            }
                            .disabled(messageText.trimmingCharacters(in: .whitespaces).isEmpty)
                        }
                        .padding()
                        .background(Color(.systemBackground))
                    }
                }
            }
            
            if showUserList {
                UserListView(
                    viewModel: viewModel,
                    selectedContact: $selectedContact,
                    isPresented: $showUserList
                )
            }
        }
        .navigationBarHidden(true)
    }
    
    private func sendMessage() {
        guard !messageText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        
        viewModel.sendMessage(to: selectedContact, body: messageText)
        messageText = ""
    }
    
    private func logout() {
        viewModel.disconnect()
        isLoggedIn = false
    }
}

struct MessageBubble: View {
    let message: MessageModel
    let isCurrentUser: Bool
    
    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            if isCurrentUser {
                Spacer()
            }
            
            VStack(alignment: isCurrentUser ? .trailing : .leading, spacing: 4) {
                Text(message.body)
                    .padding(12)
                    .background(isCurrentUser ? Color.blue : Color(.systemGray6))
                    .foregroundColor(isCurrentUser ? .white : .primary)
                    .cornerRadius(12)
                
                Text(formatTime(message.timestamp))
                    .font(.caption2)
                    .foregroundColor(.gray)
                    .padding(.horizontal, 8)
            }
            
            if !isCurrentUser {
                Spacer()
            }
        }
    }
    
    private func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

struct ChatView_Previews: PreviewProvider {
    static var previews: some View {
        ChatView(viewModel: MessageViewModel(), isLoggedIn: .constant(true))
    }
}
