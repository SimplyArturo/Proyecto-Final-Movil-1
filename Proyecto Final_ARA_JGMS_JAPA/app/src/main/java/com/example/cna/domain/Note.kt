package com.example.cna.domain

data class Note(
    val id: Int? = null,
    val title: String = "",
    val content: String = "",
    val imageUris: List<String> = emptyList(),
    val AudioUris: List<String> = emptyList(),
    val videosUris: List<String> = emptyList(),
)