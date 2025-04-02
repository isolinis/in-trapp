import SwiftUI
import shared

struct ProjectsView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    @State private var projects: [Project] = []

    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let customBlack = Color.black

    var body: some View {
        ZStack {
            customYellow.edgesIgnoringSafeArea(.all)

            if viewModel.projectsLoaded {
                if !projects.isEmpty {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(projects, id: \.id) { project in
                                ProjectRow(project: project)
                            }
                        }
                        .padding(.top, 80)
                        .padding(.horizontal, 16)
                    }
                } else {
                    Text("Proyectos no encontrados")
                        .foregroundColor(customBlack)
                        .padding(.top, 80)
                }
            } else {
                LoadingView(navigationPath: $navigationPath) // Pasa el parámetro requerido
            }

            // Botón de volver
            Button(action: {
                navigationPath.removeLast()
            }) {
                Text("<")
                    .font(.system(size: 30, weight: .bold))
                    .foregroundColor(customYellow)
                    .frame(width: 70, height: 70)
                    .background(customBlack)
                    .clipShape(Circle())
            }
            .position(x: 70, y: 70)
        }
        .onAppear {
            if !viewModel.projectsLoaded {
                loadProjects()
            }
        }
    }

    private func loadProjects() {
        Task {
            do {
                try await Api42().getProjectsWrapper()
                // Accede a los proyectos a través de userProfile
                if let userProfile = SessionManager.shared.userProfile {
                    projects = userProfile.projects.compactMap { $0 as? Project }
                }
                viewModel.projectsLoaded = true
            } catch {
                print("Error loading projects: \(error)")
                viewModel.projectsLoaded = true
            }
        }
    }
}

struct ProjectRow: View {
    let project: Project
    private let customBlack = Color.black

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(project.project.name) // Accede al nombre a través de project.project
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(customBlack)

            Text("Estado: \(project.status)")
                .font(.system(size: 14))
                .foregroundColor(customBlack)

            if let finalMark = project.finalMark {
                Text("Nota final: \(finalMark)")
                    .font(.system(size: 14))
                    .foregroundColor(customBlack)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white.opacity(0.3))
        .cornerRadius(8)
    }
}