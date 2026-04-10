package com.webforge.studio.repository

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.webforge.studio.model.OutputType
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.model.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Room Entity
// ---------------------------------------------------------------------------

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "output_type")
    val outputType: String,

    // ThemeConfig fields stored as individual columns
    @ColumnInfo(name = "theme_primary_color")
    val themePrimaryColor: String,

    @ColumnInfo(name = "theme_secondary_color")
    val themeSecondaryColor: String,

    @ColumnInfo(name = "theme_background_color")
    val themeBackgroundColor: String,

    @ColumnInfo(name = "theme_font_family")
    val themeFontFamily: String,

    @ColumnInfo(name = "theme_base_font_size_sp")
    val themeBaseFontSizeSp: Float,

    @ColumnInfo(name = "theme_is_dark_mode")
    val themeIsDarkMode: Boolean,
)

// ---------------------------------------------------------------------------
// Mapping helpers
// ---------------------------------------------------------------------------

private fun ProjectEntity.toDomain(): ProjectModel = ProjectModel(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    outputType = OutputType.entries.firstOrNull { it.name == outputType } ?: OutputType.HTML,
    themeConfig = ThemeConfig(
        primaryColor = themePrimaryColor,
        secondaryColor = themeSecondaryColor,
        backgroundColor = themeBackgroundColor,
        fontFamily = themeFontFamily,
        baseFontSizeSp = themeBaseFontSizeSp,
        isDarkMode = themeIsDarkMode,
    ),
)

private fun ProjectModel.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    outputType = outputType.name,
    themePrimaryColor = themeConfig.primaryColor,
    themeSecondaryColor = themeConfig.secondaryColor,
    themeBackgroundColor = themeConfig.backgroundColor,
    themeFontFamily = themeConfig.fontFamily,
    themeBaseFontSizeSp = themeConfig.baseFontSizeSp,
    themeIsDarkMode = themeConfig.isDarkMode,
)

// ---------------------------------------------------------------------------
// DAO
// ---------------------------------------------------------------------------

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: String)
}

// ---------------------------------------------------------------------------
// Room Database
// ---------------------------------------------------------------------------

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = true)
abstract class WebForgeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}

// ---------------------------------------------------------------------------
// Repository implementation
// ---------------------------------------------------------------------------

/**
 * Room-backed [ProjectRepository] for Android.
 *
 * Instantiated and provided by Hilt via [com.webforge.studio.di.AppModule].
 */
class ProjectRepositoryImpl @Inject constructor(
    private val dao: ProjectDao,
) : ProjectRepository {

    override fun observeAllProjects(): Flow<List<ProjectModel>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getProjectById(id: String): ProjectModel? =
        dao.getById(id)?.toDomain()

    override suspend fun upsertProject(project: ProjectModel) =
        dao.upsert(project.toEntity())

    override suspend fun deleteProject(id: String) =
        dao.deleteById(id)
}
