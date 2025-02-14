package com.example.intrapp.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.intrapp.Api42


@Composable
@Preview
fun App() {

    val context = LocalContext.current
    MaterialTheme {

        // VARIABLES OBSERVABLES:
        // Remember mutablestateof : Estados que se vigilan y al cambiar se renderiza entera la view.
        var uri by remember { mutableStateOf("") }
        //var code by remember { mutableStateOf(false) }
        //var profile by remember { mutableStateOf(false) }


        // CUERPO :

        Column(Modifier.fillMaxWidth().fillMaxHeight() , horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Button(onClick = {
                uri = Api42().getURI()

                ///DESDE AQUI

                //aqui hay que traerse toda la logica se authintra
                
                ///HASTA AQUI



            }) {
                Text("Log in with 42")

            }

        }
    }
}