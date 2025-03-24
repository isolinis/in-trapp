import SwiftUI
import AVKit
import shared

struct LoginView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    @State private var videoFinished = false
    @State private var showLoginButton = false
    @State private var player: AVPlayer?

    var body: some View {
        ZStack {
            // Video de fondo
            if let player = player {
                VideoPlayerController(
                    player: player,
                    onVideoFinished: {
                        withAnimation(.easeInOut(duration: 0.5)) {
                            showLoginButton = true
                        }
                    }
                )
                .edgesIgnoringSafeArea(.all)
            }

            // Contenido overlay
            VStack {
                Spacer()

                if showLoginButton {
                    LoginButton(
                        action: {
                            startOAuthFlow()
                        }
                    )
                    .transition(.opacity.combined(with: .scale))
                    .padding(.bottom, 50)
                }
            }
        }
        .onAppear {
            setupVideoPlayer()
        }
        .onDisappear {
            player?.pause()
        }
    }

    private func setupVideoPlayer() {
        guard let url = Bundle.main.url(forResource: "compose-resources/loginvideo", withExtension: "mp4") else {
            showLoginButton = true
            return
        }

        player = AVPlayer(url: url)
        player?.isMuted = true
        player?.actionAtItemEnd = .none

        NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: player?.currentItem,
            queue: .main
        ) { _ in
            videoFinished = true
            player?.seek(to: .zero)
            player?.play() // Loop del video
        }

        player?.play()
    }

    private func startOAuthFlow() {
        navigationPath.append("loading")
        let oauthURL = Api42().getURI()
        print("Opening OAuth URL: \(oauthURL)")

        if let url = URL(string: oauthURL) {
            UIApplication.shared.open(url)
        }
    }
}

// Componente reutilizable para el botón
struct LoginButton: View {
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            Text("LOG\nIN")
                .font(.system(size: 24, weight: .bold))
                .multilineTextAlignment(.center)
                .frame(width: 200, height: 100)
                .background(
                    LinearGradient(
                        colors: [.blue, .purple],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .foregroundColor(.white)
                .cornerRadius(15)
                .shadow(radius: 10)
        }
    }
}

// Wrapper para AVPlayerViewController
struct VideoPlayerController: UIViewControllerRepresentable {
    let player: AVPlayer
    let onVideoFinished: () -> Void

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()
        controller.player = player
        controller.showsPlaybackControls = false
        controller.videoGravity = .resizeAspectFill

        // Configurar notificación de fin de video
        NotificationCenter.default.addObserver(
            context.coordinator,
            selector: #selector(Coordinator.videoDidFinish),
            name: .AVPlayerItemDidPlayToEndTime,
            object: player.currentItem
        )

        return controller
    }

    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onVideoFinished: onVideoFinished)
    }

    class Coordinator: NSObject {
        var onVideoFinished: () -> Void

        init(onVideoFinished: @escaping () -> Void) {
            self.onVideoFinished = onVideoFinished
        }

        @objc func videoDidFinish() {
            onVideoFinished()
        }
    }
}