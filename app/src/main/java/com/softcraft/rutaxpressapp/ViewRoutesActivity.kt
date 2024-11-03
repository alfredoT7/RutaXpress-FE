package com.softcraft.rutaxpressapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.softcraft.rutaxpressapp.lineas.LineasRepository
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
    private lateinit var map: GoogleMap
    private lateinit var cvStartRoute: CardView
    private lateinit var cvEndRoute: CardView
    private lateinit var btnConfirmDirection: Button
    private var startRoutePolyline: Polyline? = null
    private var endRoutePolyline: Polyline? = null
    private var startRouteResponse: BackendRouteResponse? = null
    private var endRouteResponse: BackendRouteResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_routes)
        routeId = intent.getStringExtra("routeId")
        createFragment()
        initComponents()
        initListeners()
    }

    private fun createRoute(routeId: String?, num: Int) {
        if (routeId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = getBackendRetrofit().create(ApiService::class.java).getBackendRoute(routeId)
                    if (request.isSuccessful) {
                        request.body()?.let { response ->
                            if (num == 1) {
                                startRouteResponse = response
                            } else {
                                endRouteResponse = response
                            }
                            drawBackendRoute(response, num)
                            Log.i("alfredoDev", "Ruta obtenida del backend exitosamente")
                        }
                    } else {
                        Log.e("alfredoDev", "Error fetching route: ${request.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("alfredoDev", "Error fetching route: ${e.message}")
                }
            }
        }
    }

    private fun drawBackendRoute(routeResponse: BackendRouteResponse, num: Int) {
        LineasRepository.selectedRoutes.add(routeResponse)
        val polylineOptions = PolylineOptions()
        routeResponse.geojson.features.firstOrNull()?.geometry?.coordinates?.forEach { coordinate ->
            polylineOptions.add(LatLng(coordinate[1], coordinate[0]))
        }
        runOnUiThread {
            val polyline = map.addPolyline(polylineOptions)
            if (num == 1) {
                startRoutePolyline?.remove()
                startRoutePolyline = polyline
                polyline.color = ContextCompat.getColor(this, R.color.routeMap)
                polyline.startCap = CustomCap(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(resources, R.drawable.ini), 75, 75, false)))
                polyline.endCap = CustomCap(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(resources, R.drawable.end), 75, 75, false)))
            } else {
                endRoutePolyline?.remove()
                endRoutePolyline = polyline
                polyline.color = ContextCompat.getColor(this, R.color.teal_200)
                polyline.startCap = CustomCap(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(resources, R.drawable.ini), 75, 75, false)))
                polyline.endCap = CustomCap(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(resources, R.drawable.end), 75, 75, false)))
            }
            polyline.width = 12f
            val builder = LatLngBounds.Builder()
            polyline.points.forEach { point -> builder.include(point) }
            val bounds = builder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
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
        cvStartRoute = findViewById(R.id.cvStartRoute)
        cvEndRoute = findViewById(R.id.cvEndRoute)
        btnConfirmDirection = findViewById(R.id.btnConfirmDirection)
    }

    private fun initListeners() {
        cvStartRoute.setOnClickListener {
            endRoutePolyline?.remove()
            if (startRouteResponse != null) {
                drawBackendRoute(startRouteResponse!!, 1)
            } else {
                createRoute("$routeId-1", 1)
            }
        }
        cvEndRoute.setOnClickListener {
            startRoutePolyline?.remove()
            if (endRouteResponse != null) {
                drawBackendRoute(endRouteResponse!!, 2)
            } else {
                createRoute("$routeId-2", 2)
            }
        }
        btnConfirmDirection.setOnClickListener {
            val intent = Intent(this, InitialMapActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val cocha = LatLng(-17.39509587774758, -66.16185635257042)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cocha, 8f), 300, null)
    }
}
