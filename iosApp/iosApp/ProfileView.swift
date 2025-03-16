import SwiftUI
import shared

struct ProfileView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath

    // Obtener el perfil desde SessionManager
    let profile = SessionManager.shared.userProfile

    var body: some View {

        //VIDEO DE FONDO ??

        ZStack {
            Color.black.ignoresSafeArea() // Fondo negro

            VStack(alignment: .center, spacing: 16) {
                // AVATAR
                ZStack {
                    Circle()
                        .fill(Color.yellow)
                        .frame(width: 220, height: 220)

                    if let imageUrl = profile?.image?.link, let url = URL(string: imageUrl) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 220, height: 220)
                                    .clipShape(Circle())
                            case .failure:
                                Color.red // Muestra un color de error si la imagen no se carga
                            case .empty:
                                ProgressView() // Muestra un spinner mientras se carga la imagen
                            @unknown default:
                                EmptyView() // Manejo de casos no cubiertos
                            }
                        }
                        .frame(width: 220, height: 220)
                    } else {
                        // Si no hay URL de imagen o es inválida, muestra un fallback
                        Circle()
                            .fill(Color.gray)
                            .frame(width: 220, height: 220)
                    }
                }

                // INFO
                if let profile = profile {
                    Text(profile.login)
                        .font(.system(size: 25, weight: .bold))
                        .foregroundColor(.white)

                    Spacer().frame(height: 16) // Espaciador

                    Text("\(profile.first_name ?? "") \((profile.last_name) ?? "")")
                        .foregroundColor(.white)
                        .font(.system(size: 18))

                    Text("Email: \(profile.email )")
                        .foregroundColor(.white)
                        .font(.system(size: 18))

                    Text("Location: \(profile.location ?? "No disponible")")
                        .foregroundColor(.white)
                        .font(.system(size: 18))

                    Text("Wallet: \(profile.wallet)")
                        .foregroundColor(.white)
                        .font(.system(size: 18))
                } else {
                    Text("No profile data available")
                        .foregroundColor(.white)
                        .font(.system(size: 18))
                }

                Spacer().frame(height: 50) // Espaciador

                // Botón de proyectos
                Button(action: {
                    //viewModel.loadProjects()
                    navigationPath.append("projects")
                }) {
                    Text("PROJECTS")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.black)
                        .frame(width: 100, height: 100)
                        .background(Color.yellow)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity) // Ocupa toda la pantalla
            .padding() // Añade un padding general
        }
    }
}
