package com.kamenkarchev.timeawareness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamenkarchev.timeawareness.data.XmlRepository
import com.kamenkarchev.timeawareness.ui.screens.TimeAwarenessScreen
import com.kamenkarchev.timeawareness.ui.theme.TimeAwarenessTheme
import com.kamenkarchev.timeawareness.viewmodel.TimeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = XmlRepository(applicationContext)

        setContent {
            TimeAwarenessTheme {
                val vm = viewModel<com.kamenkarchev.timeawareness.viewmodel.TimeViewModel>(
                    factory = TimeViewModelFactory(repository)
                )
                TimeAwarenessScreen(viewModel = vm)
            }
        }
    }
}
