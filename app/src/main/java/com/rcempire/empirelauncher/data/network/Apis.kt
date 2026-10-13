package com.rcempire.empirelauncher.data.network

import com.rcempire.empirelauncher.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MojangApi {
    @GET("mc/game/version_manifest_v2.json")
    suspend fun versionManifest(): VersionManifest
}

interface ModrinthApi {
    /** facets example: [["project_type:mod"],["versions:1.21.1"],["categories:fabric"]] */
    @GET("v2/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("facets") facets: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ModrinthSearch

    @GET("v2/project/{id}/version")
    suspend fun versions(
        @Path("id") id: String,
        @Query("loaders") loaders: String,
        @Query("game_versions") gameVersions: String
    ): List<ModrinthVersion>
}
