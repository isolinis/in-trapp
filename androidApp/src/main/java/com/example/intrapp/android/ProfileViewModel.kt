package com.example.intrapp.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrapp.Api42
import com.example.intrapp.Project
import com.example.intrapp.SessionManager
import com.example.intrapp.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileViewModel : ViewModel() {

    // Estado de Perfil (false inicialmente)
    private val _profileLoaded = MutableStateFlow(false)
    val profileLoaded: StateFlow<Boolean> = _profileLoaded

    // Estado Proyectos (false inicialmente)
    private val _projectsLoaded = MutableStateFlow(false)
    val projectsLoaded: StateFlow<Boolean> = _projectsLoaded

    // Estado para skills
    private val _skillsLoaded = MutableStateFlow(false)
    val skillsLoaded: StateFlow<Boolean> = _skillsLoaded

    // Estado para errores
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    // Función para manejar el callback de OAuth
    fun handleAuthCallback(code: String) {
        viewModelScope.launch {
            try {
                println("[VIEWMODEL] HandleAuthcallback(code: $code)")

                // Llamar a handleCallback
                Api42().handleCallback(code)
                _profileLoaded.value = true

                println("[VIEWMODEL]: ${SessionManager.userProfile?.id}, ${SessionManager.userProfile?.login}, ${SessionManager.userProfile?.email}, ${SessionManager.userProfile?.location}, ${SessionManager.userProfile?.wallet}")

            } catch (e: Exception) {
                println("[VIEWMODEL]: Error al cargar el perfil: $e")
                _profileLoaded.value = false
            }
        }
    }


    // Función para cargar los proyectos
    fun loadProjects() {
        viewModelScope.launch {
            try {
                Api42().getProjects()
                _projectsLoaded.value = true
                Log.d("ViewModel", "[VIEWMODEL] Proyectos cargados: ${SessionManager.projects?.size}")
            } catch (e: Exception) {
                Log.d("ViewModel", "[VIEWMODEL] Error al cargar proyectos: ${e.message}")
                _projectsLoaded.value = false
            }
        }
    }


    fun resetState() {
        _profileLoaded.value = false
        _projectsLoaded.value = false
        _skillsLoaded.value = false
        _errorMessage.value = null
    }


}


/*
viewModelScope.launch: Ejecuta el código en un coroutine ligado al ciclo de vida del ViewModel.
 */