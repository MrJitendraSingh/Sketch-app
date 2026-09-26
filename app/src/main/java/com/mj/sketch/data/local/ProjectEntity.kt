package com.mj.sketch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val rows: Int = 1,
    val cols: Int = 1,
    val sectionIndex: Int = 0,
    val sheetWidthMm: Float = 210f,
    val sheetHeightMm: Float = 297f,
    val sheetType: String = "A4",
    val customWidthMm: String = "200",
    val customHeightMm: String = "200",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
)
