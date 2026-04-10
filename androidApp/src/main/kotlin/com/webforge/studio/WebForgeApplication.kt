package com.webforge.studio

import android.app.Application
import com.webforge.studio.repository.ProjectRepository
import com.webforge.studio.repository.ProjectRepositoryImpl

/**
 * Application class that creates and holds the app-level [ProjectRepository]
 * singleton so that ViewModels can share a single Room database instance.
 */
class WebForgeApplication : Application() {

    lateinit var projectRepository: ProjectRepository
        private set

    override fun onCreate() {
        super.onCreate()
        projectRepository = ProjectRepositoryImpl.create(this)
    }
}
