import SwiftUI

struct LoadingView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath

    var body: some View {
        ZStack {
            // Fondo amarillo (#fffc00)
            Color(red: 1.0, green: 0.988, blue: 0.0)
                .edgesIgnoringSafeArea(.all)

            // Indicador de progreso
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .black))
                .scaleEffect(2.5) // Tamaño similar a 100.dp en Android
        }
    }
}