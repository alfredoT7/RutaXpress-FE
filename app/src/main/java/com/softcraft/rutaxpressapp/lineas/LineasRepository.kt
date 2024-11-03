package com.softcraft.rutaxpressapp.lineas

import com.softcraft.rutaxpressapp.routes.BackendRouteResponse

// esta es una clase singleton que se encarga de almacenar las lineas de la aplicacion
object LineasRepository {
    var lineas: List<LineaResponse>? = null
    val selectedRoutes: MutableList<BackendRouteResponse> = mutableListOf()
}