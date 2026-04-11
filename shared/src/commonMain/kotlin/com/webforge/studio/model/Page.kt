package com.webforge.studio.model

import kotlinx.serialization.Serializable

/**
 * A page (route) belonging to a [ProjectModel].
 *
 * @param id        Unique identifier (UUID string).
 * @param projectId Owning project identifier.
 * @param name      Human-readable page name (e.g. "Home", "About").
 * @param route     URL route segment (e.g. "/", "/about").
 * @param isHome    True when this is the landing / index page.
 * @param order     Display order in the page tab strip.
 */
@Serializable
data class Page(
    val id: String,
    val projectId: String,
    val name: String,
    val route: String,
    val isHome: Boolean = false,
    val order: Int = 0,
)
