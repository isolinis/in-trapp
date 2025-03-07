package com.example.intrapp.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrapp.Api42
import com.example.intrapp.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileViewModel : ViewModel() {

    //COntenido del perfil (podria sustituir a porofileloaded una vez que el modelo de datos sea correcto)
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    // Estado de autenticación (false inicialmente)
    private val _profileLoaded = MutableStateFlow(false)
    val profileLoaded: StateFlow<Boolean> = _profileLoaded



    // Función para manejar el callback de OAuth
    fun handleAuthCallback(code: String) {
        viewModelScope.launch {
            try {
                // Obtener el perfil directamente como UserProfile
                val profile = Api42().handleCallback(code)

                Log.d("ViewModel", "true")
                _profile.value = profile
                _profileLoaded.value = true

                println("ViewModel: ${profile.id}, ${profile.login}, ${profile.email}, ${profile.location}, ${profile.wallet}")


            } catch (e: Exception) {
                Log.d("ViewModel", "false")
                _profileLoaded.value = false
            }
        }
    }

}