package com.webforge.studio.di

import android.content.Context
import androidx.room.Room
import com.webforge.studio.engine.CodeGenerator
import com.webforge.studio.engine.HtmlCodeGenerator
import com.webforge.studio.network.WebForgeHttpClient
import com.webforge.studio.repository.PageRepository
import com.webforge.studio.repository.PageRepositoryImpl
import com.webforge.studio.repository.ProjectRepository
import com.webforge.studio.repository.ProjectRepositoryImpl
import com.webforge.studio.repository.WebForgeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

/**
 * Hilt module that provides app-level singleton dependencies.
 *
 * **Why SingletonComponent?**
 * - [WebForgeDatabase] and repositories must be singletons to share a single
 *   source of truth across all ViewModels.
 * - [HttpClient] is thread-safe and expensive to create; a singleton is correct.
 * - [CodeGenerator] is stateless and safe to share.
 *
 * `fallbackToDestructiveMigration` is intentional for this alpha/dev phase so
 * schema changes don't require explicit SQL migrations during rapid iteration.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWebForgeDatabase(
        @ApplicationContext context: Context,
    ): WebForgeDatabase = Room.databaseBuilder(
        context,
        WebForgeDatabase::class.java,
        "webforge_studio.db",
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideProjectRepository(
        database: WebForgeDatabase,
    ): ProjectRepository = ProjectRepositoryImpl(database.projectDao())

    @Provides
    @Singleton
    fun providePageRepository(
        database: WebForgeDatabase,
    ): PageRepository = PageRepositoryImpl(database.pageDao())

    @Provides
    @Singleton
    fun provideCodeGenerator(): CodeGenerator = HtmlCodeGenerator()

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = WebForgeHttpClient.create()
}
