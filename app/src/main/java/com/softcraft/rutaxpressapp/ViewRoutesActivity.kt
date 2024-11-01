package com.softcraft.rutaxpressapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.softcraft.rutaxpressapp.routes.ApiService
import com.softcraft.rutaxpressapp.routes.BackendRouteResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ViewRoutesActivity : AppCompatActivity(), OnMapReadyCallback {
    private var routeId: String? = null
    private lateinit var map:GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_routes)
        createFragment()
        createRoutes()
        initComponents()
        initListeners()
    }

    private fun createRoutes() {
        val routeId = intent.getStringExtra("routeId")
        createRoute("$routeId-1",1)
        createRoute("$routeId-2",2)
    }

    private fun createRoute(routeId: String?,num:Int) {
        if (routeId != null){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = getBackendRetrofit().create(ApiService::class.java).getBackendRoute(routeId)
                    if(request.isSuccessful){
                        request.body()?.let{ response ->
                            drawBackendRoute(response, num)
                            Log.i("alfredoDev", "Ruta obtenida del backend exitosamente")
                        }
                    }else{
                        Log.e("alfredoDev", "Error fetching route: ${request.errorBody()?.string()}")
                    }
                }catch (e: Exception){
                    Log.e("alfredoDev", "Error fetching route: ${e.message}")
                }
            }
        }
    }

    private fun drawBackendRoute(routeResponse: BackendRouteResponse, num: Int) {
        val polylineOptions = PolylineOptions()
        routeResponse.geojson.features.firstOrNull()?.geometry?.coordinates?.forEach { coordinate ->
            polylineOptions.add(LatLng(coordinate[1], coordinate[0]))
        }
        runOnUiThread {
            val poly = map.addPolyline(polylineOptions)
            if (num == 1) {
                poly.color = ContextCompat.getColor(this, R.color.routeMap)
            } else {
                poly.color = ContextCompat.getColor(this, R.color.btnColor)
            }
            poly.width = 12f
        }
    }

    private fun getBackendRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://ruta-xpress-backend-express-js.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapViewRoutes) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private fun initComponents() {

    }
    private fun initListeners() {

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val cocha = LatLng(-17.39509587774758, -66.16185635257042)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cocha, 12f))
    }
}