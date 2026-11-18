package ru.itmo.tuman.features.catalog.data

import retrofit2.http.GET
import retrofit2.http.Path

interface CatalogApi {
    @GET("sets")
    suspend fun getSets(): List<CatalogSetSummaryDto>

    @GET("sets/{id}")
    suspend fun getSet(@Path("id") id: String): CatalogSetDto
}
