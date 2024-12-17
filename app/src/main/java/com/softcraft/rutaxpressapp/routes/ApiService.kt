package com.softcraft.rutaxpressapp.routes

import com.google.gson.annotations.SerializedName
import com.softcraft.rutaxpressapp.lineas.LineaResponse
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("/v2/directions/foot-walking")
    suspend fun getRouteApiService(
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

    @GET("favorites/id-routes/{userId}")
    suspend fun getFavoriteRoutesId(@Path("userId") userId: String): Response<FavoriteRoutesResponseId>

    @GET("favorites/{userId}")
    suspend fun getFavoriteRoutes(@Path("userId") userId: String): Response<FavoriteRoutesResponse>

    @GET("route-search/find-route")
    suspend fun findRoute(
        @Query("longitudeInicio") longitudeInicio: Double,
        @Query("latitudeInicio") latitudeInicio: Double,
        @Query("longitudeFinal") longitudeFinal: Double,
        @Query("latitudeFinal") latitudeFinal: Double
    ): Response<BestRouteResponse>
    @GET("driver-routes/getDriverRoute/{idUser}")
    suspend fun getDriverRoute(@Path("idUser") idUser: String): Response<DriverRouteResponse>

    @GET("descriptions/{routeId}")
    suspend fun getRouteDescription(@Path("routeId") routeId: String): Response<LineaResponse>

}
data class DriverRouteResponse(
    @SerializedName("idUser") val idUser: String,
    @SerializedName("routes") val routes: List<String>,
    @SerializedName("Placa") val placa: String
)
data class FavoriteRequest(
    val idUser: String,
    val route: String
)
data class FavoriteRoutesResponseId(
    val idUser: String,
    val favoriteRoutes: List<String>
)
data class FavoriteRoutesResponse(
    val idUser: String,
    val favoriteRoutes: List<LineaResponse>
)
data class BestRouteResponse(
    val routeId: String
)
