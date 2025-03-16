import SwiftUI

struct ProjectsView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath

    var body: some View {
        VStack {
            
                Text("PROYECTOS")
                    .font(.title2)
                    .padding()
            }

            Button(action: {
                // Volver a la pantalla de perfil
                navigationPath.removeLast()
            }) {
                Text("Volver al perfil")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
            .padding()
        }
    }

