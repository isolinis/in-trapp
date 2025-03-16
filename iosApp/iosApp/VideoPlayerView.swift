import SwiftUI
import AVKit

struct VideoPlayerView: UIViewControllerRepresentable {
    var videoName: String // Nombre del video (sin extensión)
    var onVideoFinished: () -> Void // Callback cuando el video termina

    // Crear el AVPlayerViewController
    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()

        // Cargar el video desde el bundle de la aplicación
        if let videoURL = Bundle.main.url(forResource: "videos/\(videoName)", withExtension: "mp4") {
            print("Video cargado correctamente: \(videoURL)")
            let player = AVPlayer(url: videoURL)
            controller.player = player

            // Observar cuando el video termine
            NotificationCenter.default.addObserver(
                context.coordinator,
                selector: #selector(Coordinator.videoDidFinish),
                name: .AVPlayerItemDidPlayToEndTime,
                object: player.currentItem
            )

            // Reproducir el video automáticamente
            player.play()
        } else {
            print("Error: No se pudo cargar el video \(videoName).mp4")
        }

        return controller
    }

    // Actualizar el UIViewController (no es necesario en este caso)
    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {}

    // Crear el coordinador para manejar notificaciones
    func makeCoordinator() -> Coordinator {
        Coordinator(onVideoFinished: onVideoFinished)
    }

    // Coordinador para manejar notificaciones
    class Coordinator: NSObject {
        var onVideoFinished: () -> Void

        init(onVideoFinished: @escaping () -> Void) {
            self.onVideoFinished = onVideoFinished
        }

        // Método que se llama cuando el video termina
        @objc func videoDidFinish() {
            onVideoFinished()
        }
    }
}
