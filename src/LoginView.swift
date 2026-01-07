import SwiftUI

struct LoginView: View {
    @ObservedObject var viewModel: MessageViewModel
    @Binding var isLoggedIn: Bool
    @State private var username = ""
    @State private var password = ""
    @State private var host = "localhost"
    @State private var port = "5000"
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        VStack(spacing: 20) {
            VStack(spacing: 10) {
                Text("Message App")
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.blue)
                
                Text("Connect & Chat")
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
            .padding(.bottom, 30)
            
            VStack(spacing: 15) {
                // Server Configuration
                VStack(alignment: .leading, spacing: 8) {
                    Text("Server Configuration")
                        .font(.headline)
                        .foregroundColor(.white)
                    
                    HStack {
                        Text("Host:")
                            .foregroundColor(.gray)
                        TextField("localhost", text: $host)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    
                    HStack {
                        Text("Port:")
                            .foregroundColor(.gray)
                        TextField("5000", text: $port)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .keyboardType(.numberPad)
                    }
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
                
                Divider()
                    .padding(.vertical, 10)
                
                // Login Fields
                VStack(alignment: .leading, spacing: 8) {
                    Text("Login Credentials")
                        .font(.headline)
                        .foregroundColor(.white)
                    
                    VStack(spacing: 12) {
                        TextField("Username", text: $username)
                            .padding(12)
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                        
                        SecureField("Password", text: $password)
                            .padding(12)
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                    }
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
            }
            .padding()
            
            // Login Button
            Button(action: handleLogin) {
                if viewModel.isConnecting {
                    HStack {
                        ProgressView()
                            .tint(.white)
                        Text("Connecting...")
                    }
                } else {
                    Text("Login")
                        .fontWeight(.semibold)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(12)
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(8)
            .disabled(username.isEmpty || password.isEmpty || viewModel.isConnecting)
            .padding()
            
            if showError {
                HStack {
                    Image(systemName: "exclamationmark.circle.fill")
                        .foregroundColor(.red)
                    Text(errorMessage)
                        .font(.caption)
                        .foregroundColor(.red)
                }
                .padding()
                .background(Color(.systemRed).opacity(0.2))
                .cornerRadius(8)
                .padding()
            }
            
            Spacer()
        }
        .padding()
        .background(Color(.systemBackground))
    }
    
    private func handleLogin() {
        guard !username.isEmpty && !password.isEmpty else {
            errorMessage = "Please enter username and password"
            showError = true
            return
        }
        
        viewModel.login(host: host, port: Int(port) ?? 5000, username: username, password: password) { success, message in
            if success {
                isLoggedIn = true
                showError = false
            } else {
                errorMessage = message ?? "Login failed"
                showError = true
            }
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView(viewModel: MessageViewModel(), isLoggedIn: .constant(false))
    }
}
