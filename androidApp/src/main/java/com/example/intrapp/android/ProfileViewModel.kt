package com.example.intrapp.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrapp.Api42
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    //Estado del perfil (null inicialmente)
    //private val _profileState = MutableStateFlow<String?>(null)
    //val profileState: StateFlow<String?> = _profileState

    // Estado de autenticación (false inicialmente)
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    // Función para manejar el callback de OAuth
    fun handleAuthCallback(code: String) {
        viewModelScope.launch {
            try {
                val profile = Api42().handleCallback(code)
                Log.d("ViewModel", "true")
                _isAuthenticated.value = true

            } catch (e: Exception) {
                Log.d("ViewModel", "false")
                _isAuthenticated.value = false
            }
        }
    }


}