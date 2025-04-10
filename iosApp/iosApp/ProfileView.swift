import SwiftUI
import shared

struct ProfileView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    let profile = SessionManager.shared.userProfile

    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let customBlack = Color.black

    var body: some View {
        ZStack {
            customBlack.ignoresSafeArea()

            VStack {
                ScrollView {
                    VStack(alignment: .center, spacing: 16) {
                        // AVATAR
                        ZStack {
                            Circle()
                                .fill(customYellow)
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
                                .font(.system(size: 18))
                                .foregroundColor(.white)

                            Text("email: \(profile.email)")
                                .font(.system(size: 18))
                                .foregroundColor(.white)

                            Text("Location: \(profile.location ?? "No available")")
                                .font(.system(size: 18))
                                .foregroundColor(.white)

                            Text("Wallet: \(profile.wallet)")
                                .font(.system(size: 18))
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
                                .foregroundColor(customBlack)
                                .frame(width: 100, height: 100)
                                .background(customYellow)
                                .clipShape(Circle())
                                .overlay(
                                    Circle()
                                        .stroke(customBlack, lineWidth: 2)
                                )
                        }
                        .padding(.bottom, 40)
                    }
                    .frame(maxWidth: .infinity)
                }

                // BOTÓN DE LOGOUT (texto centrado abajo)
                Button(action: {
                    SessionManager.shared.clearSession()
                    navigationPath.removeLast(navigationPath.count)
                }) {
                    Text("LOG OUT")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(customYellow)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(customBlack)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(customYellow, lineWidth: 2)
                        )
                }
                .padding(.horizontal, 40)
                .padding(.bottom, 20)
            }
        }
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
    }
}