import SwiftUI
import shared

class ProfileViewModel: ObservableObject {

    // Estado de Perfil (false inicialmente)
    @Published var profileLoaded: Bool = false
    // Estado Proyectos (false inicialmente)
    @Published var projectsLoaded: Bool = false

    // Función para manejar el callback de OAuth
    func handleAuthCallback(code: String) {
            DispatchQueue.global().async {
                do {
                    try Api42().handleCallbackWrapper(code: code)
                    DispatchQueue.main.async {
                        self.profileLoaded = true
                    }
                } catch {
                    print("Error handling auth callback: \(error)")
                    DispatchQueue.main.async {
                        self.profileLoaded = false
                    }
                }
            }
        }

        // Función para cargar los proyectos
        func loadProjects() {
            DispatchQueue.global().async {
                do {
                    try Api42().getProjectsWrapper()
                    DispatchQueue.main.async {
                        self.projectsLoaded = true
                    }
                } catch {
                    print("Error loading projects: \(error)")
                    DispatchQueue.main.async {
                        self.projectsLoaded = false
                    }
                }
            }
        }
}

/*
DispatchQueue.global().async: Ejecuta el código en un hilo en segundo plano.
DispatchQueue.main.async: Vuelve al hilo principal para actualizar el estado (profileLoaded).

do-catch (try-catch)
*/