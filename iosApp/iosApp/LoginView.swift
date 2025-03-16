import SwiftUI
import AVKit
import shared

struct LoginView: View {
    
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath // Recibe la pila de navegacion por Binding para poder navegar entre pantallas , asi como las vistas de android reciben Navcontroller
    
    // Estado para controlar si el video ha terminado
    @State private var videoFinished = false
    
    var body: some View {
        
        //VIDEO DE FONDO
        ZStack {
            if !videoFinished {
                VideoPlayerView(videoName: "loginvideo", onVideoFinished: {
                    videoFinished = true
                })
                .edgesIgnoringSafeArea(.all) // Ocupa toda la pantalla
            }
            
            if videoFinished {
                VStack {
                    
                    //BOTON
                    Button(action: {
                        // Navegar a la pantalla de carga
                        navigationPath.append("loading")
                        // Iniciar flujo OAuth (NO NECESITA EL WRAPPER????)
                        let url = Api42().getURI()
                        print("URI for OAuth: \(url)")
                        // Abrir el navegador con la URL de OAuth
                        if let url = URL(string: url) {
                            UIApplication.shared.open(url)
                        }
                        
                    }) {
                        Text("LOG\nIN")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                    }
                    .padding()
                }
            }
        }
    }
}
