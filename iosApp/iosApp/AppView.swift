import SwiftUI

//lista de pantallas (o rutas) como NavHost
enum Route: Hashable {
    case login
    case loading
    case profile
    case projects
}

struct AppView: View {

    // 1. Crear NavigationPath // Pila de navegación en SwiftUI que almacena el estado de navegacion y e historial
    @State private var navigationPath = NavigationPath()

    // 2. Obtener el ViewModel: (se inyecta en el entorno usando @EnvironmentObject)
    @EnvironmentObject private var viewModel: ProfileViewModel

    // 3. Las variables a observar no hace falta declararlas.
    // Con @Published  SwiftUI actualiza automáticamente las vistas que dependen de él.

    var body: some View {

        //4. COnfigurar NavigationStack (Como NavHost)
        // Es más moderno y flexible q NavigationView y Necesario para poder gestionar loading (eliminarla cuando carga profile)
        //Cada vista recibe navigationPath como @Binding, lo que le permite navegar a otra pantalla o retroceder
        NavigationStack(path: $navigationPath) {
                    VStack { LoginView(navigationPath: $navigationPath) } // Pantalla default LoginView
                    .navigationDestination(for: String.self) { route in
                        switch route {
                        case "loading":
                            LoadingView(navigationPath: $navigationPath)
                        case "profile":
                            ProfileView(navigationPath: $navigationPath)
                        case "projects":
                            ProjectsView(navigationPath: $navigationPath)
                        default:
                            LoginView(navigationPath: $navigationPath)
                        }
                    }
                }
                //Esto es para recoger el callback  - en Android lo recoge Mainactivity
                .onOpenURL { url in handleIncomingURL(url) }
                //5. Navegar entre pantallas
                .onChange(of: viewModel.profileLoaded) { newValue in
                    if newValue {
                        // Navegar a Profile y eliminar Loading del backstack
                        navigationPath.removeLast(navigationPath.count) // Elimina todas las pantallas
                        navigationPath.append("profile")
                    }
                    //este cacho no tienen mucho sentido no? nunca va a pasar, se quedaria en loading eternamente  porque profileloaded no cambiaria su sestado
                    //CAMBIAR A MANEJO DE ERROR CON UN TIEMOUT O ERROR
                    else {
                        // Navegar a Login y eliminar Loading del backstack
                        navigationPath.removeLast(navigationPath.count) // Elimina todas las pantallas
                        navigationPath.append("login")
                    }
                }
    }

    func handleIncomingURL(_ url: URL) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
              let code = components.queryItems?.first(where: { $0.name == "code" })?.value else {
            print("No se encontró el code en la URL")
            return
        }
        viewModel.handleAuthCallback(code: code)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        AppView()
    }
}
