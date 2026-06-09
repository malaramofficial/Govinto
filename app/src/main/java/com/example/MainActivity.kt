package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.GoVintoRepository
import com.example.ui.AppContent
import com.example.ui.GoVintoViewModel
import com.example.ui.GoVintoViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge drawing support mandatory
        enableEdgeToEdge()

        // Local SQLite Room persistence initiation
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GoVintoRepository(database.goVintoDao())
        
        // Custom ViewModel factory mapping
        val viewModel: GoVintoViewModel by viewModels {
            GoVintoViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppContent(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
