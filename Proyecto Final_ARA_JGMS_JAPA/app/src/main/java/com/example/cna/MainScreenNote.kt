package com.example.cna

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.cna.theme.Theme
import com.example.cna.ui.Screen.NoteEvent
import com.example.cna.ui.Screen.NoteScreen
import com.example.cna.ui.Screen.TareaEvent
import com.example.cna.ui.Screen.TaskViewModel
import com.example.cna.ui.Screen.taskScreen
import dagger.hilt.android.AndroidEntryPoint
import com.example.cna.ui.Screen.NoteViewModel as NoteViewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Theme  {
                val navController = rememberNavController()
                val showFloatingButtons = remember { mutableStateOf(true) } // Estado mutable para los botones

                // Listener para ocultar o mostrar botones basado en la ruta
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    showFloatingButtons.value = when (destination.route) {
                        "mainScreen" -> true // Mostrar botones solo en la pantalla principal
                        else -> false // Ocultar en otras pantallas
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Notas") },
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                titleContentColor = Color.White
                            )
                        )
                    },
                    floatingActionButton = {
                        if (showFloatingButtons.value) { // Renderizar botones solo si el estado es true
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp) // Espacio alrededor de los botones
                            ) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.Center) // Alinear los botones en el centro
                                        .padding(horizontal = 16.dp), // Espacio entre los bordes y los botones
                                    horizontalArrangement = Arrangement.spacedBy(16.dp), // Espacio entre los botones
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Botón para agregar Nota con color verde
                                    Button(
                                        onClick = {
                                            try {
                                                navController.navigate("AddEditNote/-1")
                                            } catch (e: Exception) {
                                                Log.e("MainActivity", "Navigation error: ${e.message}")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
                                    ) {
                                        Text("Agregar Nota", color = Color.White)
                                    }

                                    // Botón para agregar Tarea con color verde
                                    Button(
                                        onClick = {
                                            try {
                                                navController.navigate("AddEditTask/-1")
                                            } catch (e: Exception) {
                                                Log.e("MainActivity", "Navigation error: ${e.message}")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
                                    ) {
                                        Text("Agregar Tarea", color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                ) { innerPadding ->
                    NavigationHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToEditNote = {},
                        onNavigateBack = {}
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onNavigateToEditNote: () -> Unit,
    onNavigateBack: () -> Unit
) {
    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            MainScreen(
                modifier = modifier,
                onEditNote = { noteId ->
                    navController.navigate("AddEditNote/$noteId")
                },
                onEditTask = { taskId ->
                    navController.navigate("AddEditTask/$taskId")
                }
            )
        }
        composable("AddEditNote/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
            val viewModel: NoteViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            NoteScreen(
                state = state,
                noteId = noteId,
                onEvent = { event ->
                    when (event) {
                        is NoteEvent.NavigateBack -> {
                            navController.popBackStack()
                        }
                        else -> viewModel.onEvent(event)
                    }
                }
            )
        }
        composable("AddEditTask/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
            val taskViewModel: TaskViewModel = hiltViewModel()
            val state by taskViewModel.state.collectAsState()

            taskScreen(
                state = state,
                tareaid = taskId,
                onEvent = { event ->
                    when (event) {
                        is TareaEvent.NavigateBack -> {
                            navController.popBackStack()
                        }
                        else -> taskViewModel.onEvent(event)
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onEditNote: (Int) -> Unit,
    onEditTask: (Int) -> Unit
) {
    val context = LocalContext.current
    val noteViewModel: NoteViewModel = hiltViewModel()
    val taskViewModel: TaskViewModel = hiltViewModel()

    val notes by noteViewModel.notes.collectAsState(initial = emptyList())
    val tasks by taskViewModel.tasks.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Combina las notas y las tareas en una sola lista
        val combinedList = notes.map { (it.id ?: -1) to (it.title to it.content) } +
                tasks.map { (it.id ?: -1) to (it.title to it.content) }

        // Lista combinada de notas y tareas
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(combinedList.size) { index ->
                val (id, item) = combinedList[index]
                // Determina si es una nota o tarea y llama al Card correspondiente

                    // Si es una nota, muestra la NoteCard
                    NoteCard(
                        title = item.first,
                        content = item.second,
                        onClick = { onEditNote(id) }
                    )

                    // Si es una tarea, muestra la TaskCard
                    TaskCard(
                        title = item.first,
                        content = item.second,
                        onClick = { onEditTask(id) }
                    )

            }
        }
    }
}

@Composable
fun NoteCard(title: String, content: String, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E6F8)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                maxLines = 3,
                color = Color.Black
            )
        }
    }
}

@Composable
fun TaskCard(title: String, content: String, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E6F8)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                maxLines = 3,
                color = Color.Black
            )
        }
    }
}
