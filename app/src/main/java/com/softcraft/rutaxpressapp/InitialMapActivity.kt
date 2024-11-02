package com.softcraft.rutaxpressapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.softcraft.rutaxpressapp.lineas.LineasRepository
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
import java.io.IOException
import java.util.Locale
class InitialMapActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    private lateinit var cvBusLines: CardView
    private lateinit var cvWhereYouGoFrom: CardView
    private lateinit var cvWhereYouGoTo: CardView
    private lateinit var tvCurrentPlace: TextView
    companion object{
        const val REQUEST_CODE_LOCATION = 0
        private const val REQUEST_CODE_SEARCH_ACTIVITY = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_map)
        initListeners()
        if (isLocationPermissionGranted()) {
            createFragment()
        } else {
            requestLocationPermission()
        }
    }

    private fun drawSavedRoutes() {
        LineasRepository.selectedRoutes.forEach { routeResponse ->
            drawBackendRoute(routeResponse)
        }
    }

    private fun initListeners() {
        cvBusLines = findViewById(R.id.cvBusLines)
        cvWhereYouGoFrom = findViewById(R.id.cvWhereYouGoFrom)
        cvWhereYouGoTo = findViewById(R.id.cvWhereYouGoTo)

        cvBusLines.setOnClickListener {
            // Aquí deberías abrir la actividad de filtrado de líneas
            val click = Intent(this, LineasFilterActivity::class.java)
            startActivity(click)
        }
        cvWhereYouGoFrom.setOnClickListener{
            navigateToSearchActivity()
        }
        cvWhereYouGoTo.setOnClickListener{
            navigateToSearchActivity()
        }
        headerPlace()

    }
    fun navigateToSearchActivity(){
        val intent = Intent(this, SearchActivity::class.java)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Si la ubicación es válida, pasamos latitud y longitud en el Intent
                intent.putExtra("LATITUDE", location.latitude)
                intent.putExtra("LONGITUDE", location.longitude)
            }
            // Iniciamos `SearchActivity` ya sea con o sin coordenadas
            startActivityForResult(intent, REQUEST_CODE_SEARCH_ACTIVITY)
        }.addOnFailureListener {
            // solo lanzamos la actividad sin extras
            startActivityForResult(intent, REQUEST_CODE_SEARCH_ACTIVITY)
        }
    }

    private fun createFragment() {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun headerPlace() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tvCurrentPlace = findViewById(R.id.tvCurrentPlace)
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    try {
                        val addresses: MutableList<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                val address: Address = addresses.get(0) ?: return@addOnSuccessListener
                                var addressText:String = address.getAddressLine(0)
                                val arr: List<String> = addressText.split(",")

                                if (arr.size > 1) {
                                    var p1:String = arr[1]
                                    var p2:String = arr[2]
                                    tvCurrentPlace.text = "$p1, $p2"
                                } else {
                                    tvCurrentPlace.text = "Dirección no disponible"
                                }
                            } else {
                                tvCurrentPlace.text = "Dirección no disponible"
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        tvCurrentPlace.text = "Error al obtener la dirección"
                    }
                } else {
                    tvCurrentPlace.text = "Ubicación no disponible"
                }
            }
        } else {
            tvCurrentPlace.text = "Permiso de ubicación no concedido"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMyLocationButtonClickListener(this)
        if (isLocationPermissionGranted()) {
            enableLocation()
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        } else {
            val defaultLocation = LatLng(-34.0, 151.0)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        }
        drawSavedRoutes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SEARCH_ACTIVITY && resultCode == Activity.RESULT_OK) {
            val selectedLatitude = data?.getDoubleExtra("SELECTED_LATITUDE", -1.0)
            val selectedLongitude = data?.getDoubleExtra("SELECTED_LONGITUDE", -1.0)
            val selectedAddress = data?.getStringExtra("SELECTED_ADDRESS")

            if (selectedLatitude != null && selectedLongitude != null) {
                // Mueve tu mapa a la ubicación seleccionada
                val selectedLocation = LatLng(selectedLatitude, selectedLongitude)
                map.clear() // Limpiar marcadores existentes
                map.addMarker(MarkerOptions().position(selectedLocation).title(selectedAddress))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15f))
            }
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = LatLng(location.latitude, location.longitude)
                        val coordinates = routeResponse.geojson.features.firstOrNull()?.geometry?.coordinates
                        if (coordinates != null) {
                            val closestPoint = findClosestPointOnPolyline(userLocation, coordinates)
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

    fun findClosestPointOnPolyline(userLocation: LatLng, coordinates: List<List<Double>>): LatLng {
        var closestPoint = LatLng(coordinates[0][1], coordinates[0][0])
        var minDistance = Float.MAX_VALUE
        for (i in 0 until coordinates.size - 1) {
            val start = LatLng(coordinates[i][1], coordinates[i][0])
            val end = LatLng(coordinates[i + 1][1], coordinates[i][0])

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