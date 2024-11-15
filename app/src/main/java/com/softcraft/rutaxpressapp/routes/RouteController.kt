package com.softcraft.rutaxpressapp.routes

import android.util.Log
import com.softcraft.rutaxpressapp.service.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RouteController {
    fun buscarRoute(routeId: String, num: Int, callback: (BackendRouteResponse?)->Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getBackendRoute(routeId)
                if(response.isSuccessful){
                    callback(response.body())
                }else{
                    callback(null)
                }
            }catch (e: Exception){
                Log.e("routeErr", "Error : ${e.message}")
                callback(null)
            }
        }
    }
}