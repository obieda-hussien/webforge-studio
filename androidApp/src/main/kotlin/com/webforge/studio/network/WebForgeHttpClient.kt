package com.webforge.studio.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for the app's [HttpClient].
 *
 * **Configuration**
 * - Android engine (OkHttp-backed) for coroutine-native async I/O.
 * - [ContentNegotiation] + [Json] for automatic kotlinx.serialization
 *   encoding/decoding of request/response bodies.
 * - [Logging] at [LogLevel.BODY] in debug builds for full request tracing.
 *
 * The client is provided as a Hilt singleton from [com.webforge.studio.di.AppModule].
 */
object WebForgeHttpClient {

    fun create(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = false
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                },
            )
        }
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    android.util.Log.d("WebForgeHttp", message)
                }
            }
        }
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }
}
