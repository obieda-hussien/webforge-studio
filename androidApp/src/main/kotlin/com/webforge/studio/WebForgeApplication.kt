package com.webforge.studio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 *
 * Annotated with [@HiltAndroidApp] to trigger Hilt's code generation and
 * initialise the dependency-injection component hierarchy at app start.
 */
@HiltAndroidApp
class WebForgeApplication : Application()
