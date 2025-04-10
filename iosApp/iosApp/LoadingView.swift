import SwiftUI

struct LoadingView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath

    var body: some View {
        ZStack {
            // Fondo amarillo (#fffc00)
            Color(red: 1.0, green: 0.988, blue: 0.0)
                .ignoresSafeArea() // Versión más moderna de edgesIgnoringSafeArea

            // Indicador de progreso
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .black))
                .scaleEffect(2.5)
        }
        // Ocultar elementos de navegación
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
        .statusBarHidden(true) // Opcional: oculta también la barra de estado
    }
}