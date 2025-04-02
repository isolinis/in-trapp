import SwiftUI
import shared

struct ProfileView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    let profile = SessionManager.shared.userProfile

    // Definimos el estilo de texto para reutilizar
    private let profileTextStyle = Font.system(size: 18)

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .center, spacing: 16) {
                    // AVATAR
                    ZStack {
                        Circle()
                            .fill(Color(red: 1.0, green: 0.988, blue: 0.0)) // #fffc00
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
                                    Circle()
                                        .fill(Color.gray)
                                case .empty:
                                    ProgressView()
                                @unknown default:
                                    EmptyView()
                                }
                            }
                        } else {
                            Circle()
                                .fill(Color.gray)
                        }
                    }
                    .padding(.top, 40)

                    // INFO
                    if let profile = profile {
                        Text(profile.login)
                            .font(.system(size: 25, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.top, 16)

                        Text("\(profile.first_name ?? "") \(profile.last_name ?? "")")
                            .font(profileTextStyle)
                            .foregroundColor(.white)

                        Text("email: \(profile.email)")
                            .font(profileTextStyle)
                            .foregroundColor(.white)

                        Text("Location: \(profile.location ?? "No available")")
                            .font(profileTextStyle)
                            .foregroundColor(.white)

                        Text("Wallet: \(profile.wallet)")
                            .font(profileTextStyle)
                            .foregroundColor(.white)
                    }

                    Spacer().frame(height: 50)

                    // BOTÓN PROJECTS
                    Button(action: {
                        viewModel.loadProjects()
                        navigationPath.append("projects")
                    }) {
                        Text("PROJECTS")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.black)
                            .multilineTextAlignment(.center)
                            .frame(width: 100, height: 100)
                            .background(Color(red: 1.0, green: 0.988, blue: 0.0)) // #fffc00
                            .clipShape(Circle())
                            .overlay(
                                Circle()
                                    .stroke(Color.black, lineWidth: 2)
                            )
                    }
                    .padding(.bottom, 40)
                }
                .frame(maxWidth: .infinity)
            }
        }
    }
}