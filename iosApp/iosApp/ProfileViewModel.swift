import SwiftUI
import shared

class ProfileViewModel: ObservableObject {

    // Estado de Perfil (false inicialmente)
    @Published var profileLoaded: Bool = false
    // Estado Proyectos (false inicialmente)
    @Published var projectsLoaded: Bool = false

    @Published var projects: [Project] = []

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

        // si ya hay proyectos cargados (sincrónico)
        if let cachedProjects = SessionManager.shared.userProfile?.projects,
           !cachedProjects.isEmpty {
            DispatchQueue.main.async {
                self.projects = cachedProjects
                self.projectsLoaded = true
            }
            return
        }
        // 2. Si no, llama a la API en background
        DispatchQueue.main.async { //  Asegura ejecución en hilo principal
            Task {
                do {
                    try await Api42().getProjectsWrapper()

                    if let userProfile = SessionManager.shared.userProfile {
                        self.projects = userProfile.projects
                        self.projectsLoaded = true
                    }
                } catch {
                    print(" Error al cargar los proyectos : \(error.localizedDescription)")
                    self.projectsLoaded = false
                    self.projects = []
                }
            }
        }
    }


    }
