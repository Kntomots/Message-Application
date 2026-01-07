import SwiftUI

struct ContentView: View {
    @StateObject var viewModel = MessageViewModel()
    @State private var isLoggedIn = false
    
    var body: some View {
        ZStack {
            if isLoggedIn {
                ChatView(viewModel: viewModel, isLoggedIn: $isLoggedIn)
            } else {
                LoginView(viewModel: viewModel, isLoggedIn: $isLoggedIn)
            }
        }
        .preferredColorScheme(.dark)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
