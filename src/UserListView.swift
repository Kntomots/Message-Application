import SwiftUI

struct UserListView: View {
    @ObservedObject var viewModel: MessageViewModel
    @Binding var selectedContact: String
    @Binding var isPresented: Bool
    @State private var searchText = ""
    
    var filteredUsers: [String] {
        if searchText.isEmpty {
            return viewModel.contactList
        }
        return viewModel.contactList.filter { $0.localizedCaseInsensitiveContains(searchText) }
    }
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            VStack(spacing: 0) {
                // Header
                HStack {
                    Text("Contacts")
                        .font(.headline)
                    
                    Spacer()
                    
                    Button(action: { isPresented = false }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.headline)
                            .foregroundColor(.gray)
                    }
                }
                .padding()
                .background(Color(.systemGray6))
                
                // Search bar
                SearchBar(text: $searchText)
                    .padding()
                
                // User list
                if filteredUsers.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "person.slash")
                            .font(.system(size: 40))
                            .foregroundColor(.gray)
                        Text("No contacts found")
                            .foregroundColor(.gray)
                    }
                    .frame(maxHeight: .infinity)
                } else {
                    List(filteredUsers, id: \.self) { contact in
                        Button(action: {
                            selectedContact = contact
                            isPresented = false
                            viewModel.loadMessages(for: contact)
                        }) {
                            HStack {
                                Circle()
                                    .fill(Color.blue.opacity(0.3))
                                    .frame(width: 40, height: 40)
                                    .overlay(
                                        Text(String(contact.prefix(1)))
                                            .font(.headline)
                                            .foregroundColor(.blue)
                                    )
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(contact)
                                        .font(.headline)
                                        .foregroundColor(.primary)
                                    
                                    Text("@user")
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                }
                                
                                Spacer()
                                
                                if contact == selectedContact {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(.blue)
                                }
                            }
                            .padding(.vertical, 8)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .padding()
            .frame(maxWidth: 320)
            .shadow(radius: 10)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color.black.opacity(0.4))
        .onTapGesture {
            isPresented = false
        }
        .onAppear {
            viewModel.loadContacts()
        }
    }
}

struct SearchBar: View {
    @Binding var text: String
    
    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)
            
            TextField("Search contacts...", text: $text)
                .textFieldStyle(RoundedBorderTextFieldStyle())
            
            if !text.isEmpty {
                Button(action: { text = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                }
            }
        }
        .padding(.horizontal)
    }
}

struct UserListView_Previews: PreviewProvider {
    static var previews: some View {
        UserListView(
            viewModel: MessageViewModel(),
            selectedContact: .constant(""),
            isPresented: .constant(true)
        )
    }
}
