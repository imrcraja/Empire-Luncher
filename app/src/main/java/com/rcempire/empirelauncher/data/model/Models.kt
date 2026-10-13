package com.rcempire.empirelauncher.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class VersionManifest(val latest: Latest, val versions: List<McVersion>)
@Serializable data class Latest(val release: String, val snapshot: String)
@Serializable data class McVersion(
    val id: String, val type: String, val url: String, val sha1: String = "",
    val releaseTime: String = ""
)

@Serializable data class ModrinthSearch(val hits: List<ModrinthHit>, @SerialName("total_hits") val totalHits: Int = 0)
@Serializable data class ModrinthHit(
    @SerialName("project_id") val projectId: String,
    val slug: String = "", val title: String, val description: String = "",
    @SerialName("icon_url") val iconUrl: String? = null,
    val downloads: Int = 0,
    @SerialName("project_type") val projectType: String = "mod"
)
@Serializable data class ModrinthVersion(
    val id: String, val name: String = "",
    @SerialName("game_versions") val gameVersions: List<String> = emptyList(),
    val loaders: List<String> = emptyList(),
    val files: List<ModrinthFile> = emptyList(),
    val dependencies: List<ModrinthDependency> = emptyList()
)
@Serializable data class ModrinthFile(val url: String, val filename: String, val primary: Boolean = false, val hashes: Map<String, String> = emptyMap())
@Serializable data class ModrinthDependency(
    @SerialName("project_id") val projectId: String? = null,
    @SerialName("dependency_type") val dependencyType: String = "optional"
)
