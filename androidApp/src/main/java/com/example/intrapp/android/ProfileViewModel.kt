package com.example.intrapp.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrapp.Api42
import com.example.intrapp.Project
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

    // Estado de los proyectos
    private val _projects = MutableStateFlow<List<Project>?>(null)
    val projects: StateFlow<List<Project>?> = _projects

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


    // Función para cargar los proyectos
    fun loadProjects() {
        viewModelScope.launch {
            try {
                val projects = Api42().getProjects()
                _projects.value = projects
                _profile.value = _profile.value?.copy(projects = projects) // Actualiza UserProfile con projects
                Log.d("ViewModel", "Proyectos cargados: ${projects.size}")
            } catch (e: Exception) {
                Log.d("ViewModel", "Error al cargar proyectos: ${e.message}")
                _projects.value = null
            }
        }
    }
}
