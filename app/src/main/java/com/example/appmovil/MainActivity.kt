package com.example.appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmovil.navegations.AppNavigation
import com.example.appmovil.view.theme.AppTheme
import com.example.appmovil.viewmodels.TemaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // El tema claro/oscuro vive en DataStore (TemaViewModel)
            val temaViewModel: TemaViewModel = viewModel()
            val modoOscuro by temaViewModel.activo.collectAsStateWithLifecycle()
            AppTheme(darkTheme = modoOscuro ?: isSystemInDarkTheme()) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(temaViewModel = temaViewModel)
                }
            }
        }
    }
}
