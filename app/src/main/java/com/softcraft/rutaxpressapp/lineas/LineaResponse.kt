package com.softcraft.rutaxpressapp.lineas

import com.google.gson.annotations.SerializedName

data class LineaResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("routeId") val routeId: String,
    @SerializedName("description") val description: String
)