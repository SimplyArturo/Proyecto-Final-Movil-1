package com.example.cna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.cna.theme.Theme
import com.example.cna.ui.Screen.NoteScreen
import com.example.cna.ui.Screen.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditNote : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId = intent.getIntExtra("noteId", -1).takeIf { it != -1 } // Obtén el id de la nota si existe

        setContent {
            Theme {
                val state by viewModel.state.collectAsState()
                NoteScreen(
                    state = state,
                    noteId = noteId, // Pasa el id al componente NoteScreen
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}






