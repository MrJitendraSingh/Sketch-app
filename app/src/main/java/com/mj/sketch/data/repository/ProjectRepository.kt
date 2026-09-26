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
        rows: Int = 1,
        cols: Int = 1,
        sectionIndex: Int = 0,
        sheetWidthMm: Float = 210f,
        sheetHeightMm: Float = 297f,
    ): ProjectEntity {
        val existing = dao.getProjectByUri(imageUri)
        val now = System.currentTimeMillis()
        return if (existing != null) {
            val updated = existing.copy(
                rows = if (rows > 0) rows else existing.rows,
                cols = if (cols > 0) cols else existing.cols,
                sectionIndex = sectionIndex,
                sheetWidthMm = if (sheetWidthMm > 0f) sheetWidthMm else existing.sheetWidthMm,
                sheetHeightMm = if (sheetHeightMm > 0f) sheetHeightMm else existing.sheetHeightMm,
                lastUsedAt = now,
            )
            dao.updateProject(updated)
            updated
        } else {
            val newEntity = ProjectEntity(
                imageUri = imageUri,
                rows = rows,
                cols = cols,
                sectionIndex = sectionIndex,
                sheetWidthMm = sheetWidthMm,
                sheetHeightMm = sheetHeightMm,
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
