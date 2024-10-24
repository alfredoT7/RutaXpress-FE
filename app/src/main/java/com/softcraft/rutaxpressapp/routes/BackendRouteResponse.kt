package com.softcraft.rutaxpressapp.routes

import com.google.gson.annotations.SerializedName

data class BackendRouteResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("routeId") val routeId: String,
    @SerializedName("geojson") val geojson: GeoJson
)

data class GeoJson(
    @SerializedName("type") val type: String,
    @SerializedName("features") val features: List<GeoFeature>
)

data class GeoFeature(
    @SerializedName("type") val type: String,
    @SerializedName("geometry") val geometry: GeoGeometry
)

data class GeoGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>
)