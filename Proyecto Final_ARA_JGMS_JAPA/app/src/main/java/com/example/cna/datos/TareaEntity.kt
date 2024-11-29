package com.example.cna.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TareaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = 0,
    val title: String,
    val content: String,
    val imageUris: List<String> = emptyList(),
    val AudioUris: List<String> = emptyList(),
    val videosUris: List<String> = emptyList(),
    val dateTimeMillis: Long?,
    val isCompleted: Boolean = false
)
