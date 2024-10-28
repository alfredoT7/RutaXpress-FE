package com.softcraft.rutaxpressapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.softcraft.rutaxpressapp.routes.ApiService
import com.softcraft.rutaxpressapp.routes.BackendRouteResponse
import com.softcraft.rutaxpressapp.routes.CalculadoraDist
import com.softcraft.rutaxpressapp.routes.RouteResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class InitialMapActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    private lateinit var cvBusLines: CardView
    companion object{
        const val REQUEST_CODE_LOCATION = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_map)
        initListeners()
        // Solicitar permisos antes de crear el mapa
        if (isLocationPermissionGranted()) {
            createFragment()
            val routeId = intent.getStringExtra("routeId")
            if (routeId != null) {
                createRoute(routeId)
            }
        } else {
            requestLocationPermission()  // Esto debería solicitar los permisos
        }
    }
    private fun initListeners() {
        cvBusLines = findViewById(R.id.cvBusLines)
        cvBusLines.setOnClickListener {
            val click = Intent(this, LineasFilterActivity::class.java)
            startActivity(click)
        }
    }
    private fun createFragment() {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) // implementamos OnMapReadyCallback
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMyLocationButtonClickListener(this)
        // Intentamos obtener la ubicación actual
        if (isLocationPermissionGranted()) {
            enableLocation()
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Movemos la cámara a la ubicación del usuario con un nivel de zoom
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f)) // Ajusta el zoom según tu preferencia
                }
            }
        } else {
            // Si no hay permiso, muéstralo en la posición lejana
            val defaultLocation = LatLng(-34.0, 151.0) // Ubicación por defecto, reemplaza con la que desees
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f)) // Ajusta el zoom según tu preferencia
        }
    }
    private fun isLocationPermissionGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        // Mostrar la solicitud de permiso al usuario
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso fue concedido, habilitar la localización
                createFragment()
            } else {
                // El permiso fue denegado
                Toast.makeText(this, "Acepta los permisos de localización", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Acepta los permisos de localización", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón de ubicación pulsado", Toast.LENGTH_SHORT).show()
        return false
    }
    private fun createRoute(routeId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = getBackendRetrofit().create(ApiService::class.java)
                    .getBackendRoute(routeId)
                if (request.isSuccessful) {
                    request.body()?.let { response ->
                        drawBackendRoute(response)
                        Log.i("alfredoDev", "Backend route fetched successfully")
                    }
                } else {
                    Log.e("alfredoDev", "Error fetching route: ${request.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("alfredoDev", "Exception fetching route", e)
            }
        }
    }
    private fun drawBackendRoute(routeResponse: BackendRouteResponse) {
        val polylineOptions = PolylineOptions()
        routeResponse.geojson.features.firstOrNull()?.geometry?.coordinates?.forEach { coordinate ->
            polylineOptions.add(LatLng(coordinate[1], coordinate[0]))
        }
        runOnUiThread {
            val poly = map.addPolyline(polylineOptions)
            poly.color = ContextCompat.getColor(this, R.color.btnColor)
            poly.width = 12f
            poly.endCap = CustomCap(resizeIcon(R.drawable.bus, this, 50, 50))
            // Obtener la ubicación actual
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = LatLng(location.latitude, location.longitude)
                        val coordinates = routeResponse.geojson.features.firstOrNull()?.geometry?.coordinates
                        if (coordinates != null) {
                            val closestPoint = findClosestPointOnPolyline(userLocation, coordinates)
                            // Agregar un marcador en el punto más cercano
                            map.addMarker(
                                MarkerOptions()
                                    .position(closestPoint)
                                    .title("Parada más cercana")
                                    .icon(resizeIcon(R.drawable.bus_stop, this, 100, 100))
                            )
                            val bounds = LatLngBounds.Builder()
                                .include(userLocation)
                                .include(closestPoint)
                            coordinates.forEach { coordinate ->
                                bounds.include(LatLng(coordinate[1], coordinate[0]))
                            }
                            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                        }
                    }
                }
            }
        }
    }
    private fun resizeIcon(resourceId: Int, context: Context, width: Int, height: Int): BitmapDescriptor {
        val imageBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }
    private fun getBackendRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://ruta-xpress-backend-express-js.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun findClosestPointOnPolyline(userLocation: LatLng, coordinates: List<List<Double>>): LatLng {
        var closestPoint = LatLng(coordinates[0][1], coordinates[0][0])
        var minDistance = Float.MAX_VALUE
        for (i in 0 until coordinates.size - 1) {
            val start = LatLng(coordinates[i][1], coordinates[i][0])
            val end = LatLng(coordinates[i + 1][1], coordinates[i + 1][0])

            val closestPointOnSegment = findClosestPointOnSegment(userLocation, start, end)

            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                closestPointOnSegment.latitude, closestPointOnSegment.longitude,
                results
            )
            if (results[0] < minDistance) {
                minDistance = results[0]
                closestPoint = closestPointOnSegment
            }
        }

        return closestPoint
    }

    private fun findClosestPointOnSegment(p: LatLng, start: LatLng, end: LatLng): LatLng {
        val dx = end.longitude - start.longitude
        val dy = end.latitude - start.latitude

        if (dx == 0.0 && dy == 0.0) {
            return start
        }

        val t = ((p.longitude - start.longitude) * dx + (p.latitude - start.latitude) * dy) /
                (dx * dx + dy * dy)

        if (t < 0) return start
        if (t > 1) return end

        return LatLng(
            start.latitude + t * dy,
            start.longitude + t * dx
        )
    }
}