package com.dopey.timeawarenessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dopey.timeawarenessapp.data.XmlRepository
import com.dopey.timeawarenessapp.ui.screens.TimeAwarenessScreen
import com.dopey.timeawarenessapp.ui.theme.TimeAwarenessTheme
import com.dopey.timeawarenessapp.viewmodel.TimeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = XmlRepository(applicationContext)
        setContent {
            TimeAwarenessTheme {
                val vm = viewModel<com.dopey.timeawarenessapp.viewmodel.TimeViewModel>(
                    factory = TimeViewModelFactory(repository)
                )
                TimeAwarenessScreen(viewModel = vm)
            }
        }
    }
}
