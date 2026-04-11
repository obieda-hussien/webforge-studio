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
import androidx.room.Transaction
import com.webforge.studio.model.BlockChain
import com.webforge.studio.model.BlockEventType
import com.webforge.studio.model.BlockNode
import com.webforge.studio.model.BlockType
import com.webforge.studio.model.OutputType
import com.webforge.studio.model.ProjectModel
import com.webforge.studio.model.TargetPlatform
import com.webforge.studio.model.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Room Entity — Project
// ---------------------------------------------------------------------------

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "slug")
    val slug: String = "",

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "output_type")
    val outputType: String,

    @ColumnInfo(name = "target_platform")
    val targetPlatform: String = TargetPlatform.HTML.name,

    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null,

    @ColumnInfo(name = "color_seed")
    val colorSeed: Long = 0xFF6750A4,

    @ColumnInfo(name = "font_pair")
    val fontPair: String = "Inter / Roboto",

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
// Mapping helpers — Project
// ---------------------------------------------------------------------------

private fun ProjectEntity.toDomain(): ProjectModel = ProjectModel(
    id = id,
    name = name,
    slug = slug,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    outputType = OutputType.entries.firstOrNull { it.name == outputType } ?: OutputType.HTML,
    targetPlatform = TargetPlatform.entries.firstOrNull { it.name == targetPlatform } ?: TargetPlatform.HTML,
    thumbnailPath = thumbnailPath,
    colorSeed = colorSeed,
    fontPair = fontPair,
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
    slug = slug,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    outputType = outputType.name,
    targetPlatform = targetPlatform.name,
    thumbnailPath = thumbnailPath,
    colorSeed = colorSeed,
    fontPair = fontPair,
    themePrimaryColor = themeConfig.primaryColor,
    themeSecondaryColor = themeConfig.secondaryColor,
    themeBackgroundColor = themeConfig.backgroundColor,
    themeFontFamily = themeConfig.fontFamily,
    themeBaseFontSizeSp = themeConfig.baseFontSizeSp,
    themeIsDarkMode = themeConfig.isDarkMode,
)

// ---------------------------------------------------------------------------
// DAO — Project
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
// Room Entity — Page
// ---------------------------------------------------------------------------

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "project_id")
    val projectId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "route")
    val route: String,

    @ColumnInfo(name = "is_home")
    val isHome: Boolean = false,

    @ColumnInfo(name = "page_order")
    val order: Int = 0,
)

// ---------------------------------------------------------------------------
// Mapping helpers — Page
// ---------------------------------------------------------------------------

private fun PageEntity.toDomain() = com.webforge.studio.model.Page(
    id = id,
    projectId = projectId,
    name = name,
    route = route,
    isHome = isHome,
    order = order,
)

private fun com.webforge.studio.model.Page.toEntity() = PageEntity(
    id = id,
    projectId = projectId,
    name = name,
    route = route,
    isHome = isHome,
    order = order,
)

// ---------------------------------------------------------------------------
// DAO — Page
// ---------------------------------------------------------------------------

@Dao
interface PageDao {

    @Query("SELECT * FROM pages WHERE project_id = :projectId ORDER BY page_order ASC")
    fun observeByProject(projectId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PageEntity)

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun deleteById(id: String)
}

// ---------------------------------------------------------------------------
// Room Entity — Block Chains / Nodes
// ---------------------------------------------------------------------------

@Entity(tableName = "block_chains")
data class BlockChainEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "element_id")
    val elementId: String?,
    @ColumnInfo(name = "event_type")
    val eventType: String,
)

@Entity(tableName = "block_nodes")
data class BlockNodeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "chain_id")
    val chainId: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "node_order")
    val order: Int,
    @ColumnInfo(name = "parameters_json")
    val parametersJson: String,
    @ColumnInfo(name = "connected_chain_id")
    val connectedChainId: String?,
    @ColumnInfo(name = "parent_block_id")
    val parentBlockId: String?,
    @ColumnInfo(name = "is_disabled")
    val isDisabled: Boolean,
)

private val blockJson = Json { ignoreUnknownKeys = true }

private fun BlockChainEntity.toDomain(nodes: List<BlockNode>) = BlockChain(
    id = id,
    elementId = elementId,
    eventType = BlockEventType.entries.firstOrNull { it.name == eventType } ?: BlockEventType.ON_CLICK,
    blocks = nodes.sortedBy { it.order },
)

private fun BlockChain.toEntity() = BlockChainEntity(
    id = id,
    elementId = elementId,
    eventType = eventType.name,
)

private fun BlockNodeEntity.toDomain() = BlockNode(
    id = id,
    chainId = chainId,
    type = BlockType.entries.firstOrNull { it.name == type } ?: BlockType.CONSOLE_LOG,
    order = order,
    parameters = runCatching {
        blockJson.decodeFromString<Map<String, JsonPrimitive>>(parametersJson)
    }.getOrDefault(emptyMap()),
    connectedChainId = connectedChainId,
    parentBlockId = parentBlockId,
    isDisabled = isDisabled,
)

private fun BlockNode.toEntity() = BlockNodeEntity(
    id = id,
    chainId = chainId,
    type = type.name,
    order = order,
    parametersJson = blockJson.encodeToString(parameters),
    connectedChainId = connectedChainId,
    parentBlockId = parentBlockId,
    isDisabled = isDisabled,
)

@Dao
interface BlockChainDao {
    @Query("SELECT * FROM block_chains WHERE ((:elementId IS NULL AND element_id IS NULL) OR element_id = :elementId) ORDER BY id ASC")
    fun observeByElement(elementId: String?): Flow<List<BlockChainEntity>>

    @Query("SELECT * FROM block_chains WHERE id = :chainId LIMIT 1")
    suspend fun getById(chainId: String): BlockChainEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chain: BlockChainEntity)

    @Query("DELETE FROM block_chains WHERE id = :chainId")
    suspend fun deleteById(chainId: String)
}

@Dao
interface BlockNodeDao {
    @Query("SELECT * FROM block_nodes WHERE chain_id = :chainId ORDER BY node_order ASC")
    fun observeByChain(chainId: String): Flow<List<BlockNodeEntity>>

    @Query("SELECT * FROM block_nodes WHERE chain_id = :chainId ORDER BY node_order ASC")
    suspend fun getByChain(chainId: String): List<BlockNodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(node: BlockNodeEntity)

    @Query("DELETE FROM block_nodes WHERE id = :nodeId")
    suspend fun deleteById(nodeId: String)

    @Query("DELETE FROM block_nodes WHERE chain_id = :chainId")
    suspend fun deleteByChain(chainId: String)
}

// ---------------------------------------------------------------------------
// Room Database (version 2 — adds pages table + new project columns)
// ---------------------------------------------------------------------------

@Database(
    entities = [ProjectEntity::class, PageEntity::class, BlockChainEntity::class, BlockNodeEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class WebForgeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun pageDao(): PageDao
    abstract fun blockChainDao(): BlockChainDao
    abstract fun blockNodeDao(): BlockNodeDao
}

// ---------------------------------------------------------------------------
// Repository implementations
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

/**
 * Room-backed [PageRepository] for Android.
 *
 * Instantiated and provided by Hilt via [com.webforge.studio.di.AppModule].
 */
class PageRepositoryImpl @Inject constructor(
    private val dao: PageDao,
) : PageRepository {

    override fun observeByProject(projectId: String): Flow<List<com.webforge.studio.model.Page>> =
        dao.observeByProject(projectId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): com.webforge.studio.model.Page? =
        dao.getById(id)?.toDomain()

    override suspend fun upsertPage(page: com.webforge.studio.model.Page) =
        dao.upsert(page.toEntity())

    override suspend fun deletePage(id: String) =
        dao.deleteById(id)
}

class BlockRepositoryImpl @Inject constructor(
    private val chainDao: BlockChainDao,
    private val nodeDao: BlockNodeDao,
) : BlockRepository {

    override fun observeChainsByElement(elementId: String?): Flow<List<BlockChain>> =
        chainDao.observeByElement(elementId).map { chains ->
            chains.map { chain ->
                val nodes = nodeDao.observeByChain(chain.id).first().map { it.toDomain() }
                chain.toDomain(nodes)
            }
        }

    override suspend fun getChainsByElement(elementId: String?): List<BlockChain> =
        observeChainsByElement(elementId).first()

    override suspend fun getChainById(chainId: String): BlockChain? {
        val chain = chainDao.getById(chainId) ?: return null
        val nodes = nodeDao.getByChain(chainId).map { it.toDomain() }
        return chain.toDomain(nodes)
    }

    override suspend fun upsertChain(chain: BlockChain) {
        chainDao.upsert(chain.toEntity())
        replaceNodes(chain.id, chain.blocks)
    }

    @Transaction
    override suspend fun deleteChain(chainId: String) {
        nodeDao.deleteByChain(chainId)
        chainDao.deleteById(chainId)
    }

    override fun observeNodes(chainId: String): Flow<List<BlockNode>> =
        nodeDao.observeByChain(chainId).map { list -> list.map { it.toDomain() } }

    override suspend fun upsertNode(node: BlockNode) {
        nodeDao.upsert(node.toEntity())
    }

    override suspend fun deleteNode(nodeId: String) {
        nodeDao.deleteById(nodeId)
    }

    @Transaction
    override suspend fun replaceNodes(chainId: String, nodes: List<BlockNode>) {
        nodeDao.deleteByChain(chainId)
        nodes.sortedBy { it.order }.forEach { nodeDao.upsert(it.copy(chainId = chainId).toEntity()) }
    }
}
