package com.softcraft.rutaxpressapp.routes

import com.softcraft.rutaxpressapp.lineas.LineaResponse
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("/v2/directions/driving-car")
    suspend fun getRoute(
        @Query("api_key") key: String,
        @Query("start", encoded = true) start: String,
        @Query("end", encoded = true) end: String
    ):Response<RouteResponse>

    @GET("routes/{routeId}")
    suspend fun getBackendRoute(
        @Path("routeId") routeId: String
    ): Response<BackendRouteResponse>
    @GET("/descriptions/")
    suspend fun getLineas(): Response<List<LineaResponse>>

    @POST("favorites/add")
    suspend fun addFavorites(@Body request: FavoriteRequest): Response<Void>

    @POST("favorites/remove")
    suspend fun removeFavorite(@Body request: FavoriteRequest): Response<Void>
}
data class FavoriteRequest(
    val idUser: String,
    val route: String
)