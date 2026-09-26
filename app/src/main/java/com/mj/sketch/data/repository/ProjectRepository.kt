package com.mj.sketch.data.repository

import android.content.Context
import com.mj.sketch.data.local.AppDatabase
import com.mj.sketch.data.local.ProjectDao
import com.mj.sketch.data.local.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun getProjectByUri(uri: String): ProjectEntity? {
        return dao.getProjectByUri(uri)
    }

    suspend fun saveOrUpdateProject(
        imageUri: String,
        rows: Int = -1,
        cols: Int = -1,
        sectionIndex: Int = -1,
        sheetWidthMm: Float = -1f,
        sheetHeightMm: Float = -1f,
        sheetType: String? = null,
        customWidthMm: String? = null,
        customHeightMm: String? = null,
    ): ProjectEntity {
        val existing = dao.getProjectByUri(imageUri)
        val now = System.currentTimeMillis()
        return if (existing != null) {
            val updated = existing.copy(
                rows = if (rows > 0) rows else existing.rows,
                cols = if (cols > 0) cols else existing.cols,
                sectionIndex = if (sectionIndex >= 0) sectionIndex else existing.sectionIndex,
                sheetWidthMm = if (sheetWidthMm > 0f) sheetWidthMm else existing.sheetWidthMm,
                sheetHeightMm = if (sheetHeightMm > 0f) sheetHeightMm else existing.sheetHeightMm,
                sheetType = sheetType ?: existing.sheetType,
                customWidthMm = customWidthMm ?: existing.customWidthMm,
                customHeightMm = customHeightMm ?: existing.customHeightMm,
                lastUsedAt = now,
            )
            dao.updateProject(updated)
            updated
        } else {
            val newEntity = ProjectEntity(
                imageUri = imageUri,
                rows = if (rows > 0) rows else 1,
                cols = if (cols > 0) cols else 1,
                sectionIndex = if (sectionIndex >= 0) sectionIndex else 0,
                sheetWidthMm = if (sheetWidthMm > 0f) sheetWidthMm else 210f,
                sheetHeightMm = if (sheetHeightMm > 0f) sheetHeightMm else 297f,
                sheetType = sheetType ?: "A4",
                customWidthMm = customWidthMm ?: "200",
                customHeightMm = customHeightMm ?: "200",
                createdAt = now,
                lastUsedAt = now,
            )
            val newId = dao.insertProject(newEntity)
            newEntity.copy(id = newId)
        }
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: ProjectRepository? = null

        fun getInstance(context: Context): ProjectRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getDatabase(context)
                val instance = ProjectRepository(database.projectDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
