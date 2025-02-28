import SwiftUI
import shared

struct ContentView: View {
    @State private var url: String = Api42().getURI()

    var body: some View {
        VStack {
            Button(action: {
                if let url = URL(string: url) {
                    UIApplication.shared.open(url)
                }
            }) {
                Text("Login with 42 credentials")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
                .padding()
        }
        .onOpenURL { url in handleIncomingURL(url) }
    }
}

func handleIncomingURL(_ url: URL) {
    guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
    let code = components.queryItems?.first(where: { $0.name == "code" })?.value else {
        print("No se encontró el código en la URL")
        return
    }
    do {
        try Api42().handleCallbackWrapper(code: code)
        print("Callback manejado correctamente")
    } catch {
        print("Error al manejar el callback: \(error)")
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
