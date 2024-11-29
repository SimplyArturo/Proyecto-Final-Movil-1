package com.example.cna.ui.Screen

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.cna.R
import com.example.cna.componentes.AudioRecorderButton
import com.example.cna.componentes.CameraButton
import com.example.cna.componentes.VideoCaptureButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    state: NoteState,
    noteId: Int?,
    onEvent: (NoteEvent) -> Unit,
) {
    val viewModel: NoteViewModel = hiltViewModel()
    LaunchedEffect(noteId) {
        noteId?.let { viewModel.loadNoteById(it) }
    }

    val context = LocalContext.current
    var mediaPlayer: MediaPlayer? = remember { null }

    val (previewUri, setPreviewUri) = remember { mutableStateOf<String?>(null) }
    val (isVideo, setIsVideo) = remember { mutableStateOf(false) }
    @Composable
    fun FullScreenPreview(
        uri: String,
        isVideo: Boolean,
        onDismiss: () -> Unit
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (isVideo) {
                    AndroidView(
                        factory = { ctx ->
                            androidx.media3.ui.PlayerView(ctx).apply {
                                val exoPlayer = androidx.media3.exoplayer.ExoPlayer.Builder(ctx).build().apply {
                                    setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                                    prepare()
                                    playWhenReady = true
                                }
                                player = exoPlayer
                                addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                                    override fun onViewAttachedToWindow(view: View) {}
                                    override fun onViewDetachedFromWindow(view: View) {
                                        exoPlayer.release()
                                    }
                                })
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.close),
                        tint = Color.White
                    )
                }
            }
        }
    }

    fun playAudio(uri: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(uri))
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayback", "Error al reproducir el audio", e)
        }
    }

    if (previewUri != null) {
        FullScreenPreview(
            uri = previewUri,
            isVideo = isVideo,
            onDismiss = { setPreviewUri(null) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_note)) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF3F51B5),
                    titleContentColor = Color.White
                )
            )
        },
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo de Título
            item {
                Text(
                    text = stringResource(id = R.string.title),
                    style = TextStyle(fontSize = 18.sp, color = Color.Black),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BasicTextField(
                    value = state.title,
                    onValueChange = { onEvent(NoteEvent.TitleChange(it)) },
                    textStyle = TextStyle(fontSize = 20.sp, color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, MaterialTheme.shapes.small)
                        .padding(16.dp),
                    decorationBox = { innerTextField ->
                        if (state.title.isEmpty()) {
                            Text(text = stringResource(id = R.string.enter_title), color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
            }

            // Campo de Contenido
            item {
                Text(
                    text = stringResource(id = R.string.content),
                    style = TextStyle(fontSize = 18.sp, color = Color.Black),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BasicTextField(
                    value = state.content,
                    onValueChange = { onEvent(NoteEvent.ContentChange(it)) },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray, MaterialTheme.shapes.small)
                        .padding(16.dp),
                    decorationBox = { innerTextField ->
                        if (state.content.isEmpty()) {
                            Text(text = stringResource(id = R.string.enter_content), color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
            }

            // Botones para multimedia
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CameraButton(onImagesCaptured = { uri ->
                        uri?.let {
                            if (!state.imageUris.contains(it.toString())) {
                                onEvent(NoteEvent.AddImage(it.toString()))
                            }
                        }
                    })

                    VideoCaptureButton(onVideoCaptured = { uri ->
                        uri?.let {
                            if (!state.videosUris.contains(it.toString())) {
                                onEvent(NoteEvent.AddVideo(it.toString()))
                            }
                        }
                    })
                    AudioRecorderButton(onAudiosCaptured = { uris ->
                        uris.forEach { uri ->
                            if (!state.AudioUris.contains(uri.toString())) {
                                onEvent(NoteEvent.AddAudio(uri.toString()))
                            }
                        }
                    })

                }
            }

            val validAudios = state.AudioUris.filter { it.isNotEmpty() }
            if (validAudios.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.audio),
                        style = TextStyle(fontSize = 18.sp, color = Color.Black),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(validAudios) { uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.LightGray),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uri.split("/").last(), // Muestra solo el nombre del archivo
                            style = TextStyle(fontSize = 16.sp, color = Color.Black),
                            modifier = Modifier.padding(8.dp)
                        )
                        Row {
                            Button(
                                onClick = { playAudio(uri) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(text = stringResource(id = R.string.play))
                            }
                            IconButton(
                                onClick = { onEvent(NoteEvent.RemoveAudio(uri)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.delete_audio),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            val validImages = state.imageUris.filter { it.isNotEmpty() }
            if (validImages.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.saved_images),
                        style = TextStyle(fontSize = 18.sp, color = Color.Black),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(validImages) { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(id = R.string.saved_images),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable {
                                    setPreviewUri(uri)
                                    setIsVideo(false)
                                },
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onEvent(NoteEvent.RemoveImage(uri)) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_image),
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            val validVideos = state.videosUris.filter { it.isNotEmpty() }
            if (validVideos.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.saved_videos),
                        style = TextStyle(fontSize = 18.sp, color = Color.Black),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(validVideos) { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                androidx.media3.ui.PlayerView(ctx).apply {
                                    val exoPlayer = androidx.media3.exoplayer.ExoPlayer.Builder(ctx).build().apply {
                                        setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                                        prepare()
                                        playWhenReady = false
                                    }
                                    player = exoPlayer
                                    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                                        override fun onViewAttachedToWindow(view: View) {}
                                        override fun onViewDetachedFromWindow(view: View) {
                                            exoPlayer.release()
                                        }
                                    })
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { onEvent(NoteEvent.RemoveVideo(uri)) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_video),
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
            // Botones de Guardar y Eliminar al final
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            onEvent(NoteEvent.Save)
                            onEvent(NoteEvent.NavigateBack)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.save_note))
                    }

                    Button(
                        onClick = {
                            onEvent(NoteEvent.DeleteNote)
                            onEvent(NoteEvent.NavigateBack)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.delete_note))
                    }
                }
            }
        }
    }
}
