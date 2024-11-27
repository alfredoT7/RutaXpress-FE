package com.softcraft.rutaxpressapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.auth.User
import com.softcraft.rutaxpressapp.lineas.LineasRepository
import com.softcraft.rutaxpressapp.routes.ApiService
import com.softcraft.rutaxpressapp.routes.BackendRouteResponse
import com.softcraft.rutaxpressapp.routes.RouteResponse
import com.softcraft.rutaxpressapp.service.ApiClient
import com.softcraft.rutaxpressapp.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Locale

class InitialMapActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    private lateinit var cvFavoriteRoutes: CardView
    private lateinit var cvBusLines: CardView
    private lateinit var cvWhereYouGoFrom: CardView
    private lateinit var cvWhereYouGoTo: CardView
    private lateinit var tvCurrentPlace: TextView
    private lateinit var tvUserName: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnSearchTrufi:Button
    private var currentPolyline: Polyline? = null
    private var currentMarker: Marker? = null
    private var fromLocation: LatLng? = null
    private var toLocation: LatLng? = null
    private var fromMarker: Marker? = null
    private var toMarker: Marker? = null

    companion object {
        private const val REQUEST_CODE_SEARCH_FROM = 1
        private const val REQUEST_CODE_SEARCH_TO = 2
        private const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_map)
        initComponents()
        loadUserProfile()
        initListeners()
        if (isLocationPermissionGranted()) {
            createFragment()
        } else {
            requestLocationPermission()
        }
    }

    private fun initComponents() {
        cvFavoriteRoutes = findViewById(R.id.cvFavoriteRoutes)
        cvBusLines = findViewById(R.id.cvBusLines)
        cvWhereYouGoFrom = findViewById(R.id.cvWhereYouGoFrom)
        cvWhereYouGoTo = findViewById(R.id.cvWhereYouGoTo)
        tvCurrentPlace = findViewById(R.id.tvCurrentPlace)
        tvUserName = findViewById(R.id.tvUserName)
        imgProfile = findViewById(R.id.imgProfile)
        btnSearchTrufi = findViewById(R.id.btnSearchTrufi)
    }

    private fun drawSavedRoutes() {
        LineasRepository.selectedRoutes.forEach { routeResponse ->
            drawBackendRoute(routeResponse)
        }
    }

    private fun loadUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("username", "Usuario")
        val profileImageUrl = sharedPref.getString("profileImageUrl", null)

        // Mostrar nombre del usuario
        tvUserName.text = userName
        // Mostrar imagen del perfil usando Glide
        if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_profile)
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.ic_default_profile)
        }
    }

    private fun initListeners() {
        cvFavoriteRoutes.setOnClickListener {
            val click = Intent(this, FavoriteRoutesActivity::class.java)
            startActivity(click)
        }
        cvBusLines.setOnClickListener {
            val click = Intent(this, LineasFilterActivity::class.java)
            startActivity(click)
        }
        cvWhereYouGoFrom.setOnClickListener { navigateToSearchActivity(REQUEST_CODE_SEARCH_FROM) }
        cvWhereYouGoTo.setOnClickListener { navigateToSearchActivity(REQUEST_CODE_SEARCH_TO) }
        headerPlace()
        btnSearchTrufi.setOnClickListener {
            queryBestRoute()
        }
    }
    private fun queryBestRoute(){
        val fromLocation = UserRepository.userFromLocation
        val toLocation = UserRepository.userToLocation
        if(fromLocation != null && toLocation != null){
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    val response = ApiClient.apiService.findRoute(fromLocation.longitude, fromLocation.latitude, toLocation.longitude, toLocation.latitude)

                    if(response.isSuccessful){
                        val routeId = response.body()?.routeId
                        if(routeId!=null){
                            runOnUiThread{
                                Toast.makeText(this@InitialMapActivity, "$routeId",Toast.LENGTH_LONG).show()
                            }
                            val backendRouteResponse = ApiClient.apiService.getBackendRoute(routeId)
                            if(backendRouteResponse.isSuccessful){
                                val routeResponse = backendRouteResponse.body()
                                if (routeResponse != null){
                                    runOnUiThread { drawBackendRoute(routeResponse) }
                                }else {
                                    runOnUiThread {
                                        Toast.makeText(this@InitialMapActivity, "Error al obtener la ruta", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }else{
                                runOnUiThread {
                                    Toast.makeText(this@InitialMapActivity, "Error al obtener la ruta", Toast.LENGTH_LONG).show()
                                }
                            }
                        }else{
                            runOnUiThread {
                                Toast.makeText(this@InitialMapActivity, "Coordenadas inválidas", Toast.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        runOnUiThread {
                            Toast.makeText(this@InitialMapActivity, "Coordenadas inválidas", Toast.LENGTH_LONG).show()
                        }
                    }
                }catch (e :Exception){
                    runOnUiThread {
                        Toast.makeText(this@InitialMapActivity, "Coordenadas inválidas", Toast.LENGTH_LONG).show()
                    }   
                }

            }
        }else{
            Toast.makeText(this, "una de las coordenadas falta", Toast.LENGTH_SHORT).show()
        }
    }
    fun navigateToSearchActivity(requestCode: Int) {
        val intent = Intent(this, SearchActivity::class.java)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                intent.putExtra("LATITUDE", location.latitude)
                intent.putExtra("LONGITUDE", location.longitude)
            }
            startActivityForResult(intent, requestCode)
        }.addOnFailureListener {
            startActivityForResult(intent, requestCode)
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
                                val address: Address = addresses[0] ?: return@addOnSuccessListener
                                val addressText: String = address.getAddressLine(0)
                                val arr: List<String> = addressText.split(",")

                                if (arr.size > 1) {
                                    val p1: String = arr[1]
                                    val p2: String = arr[2]
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

        if (resultCode == Activity.RESULT_OK) {
            val selectedLatitude = data?.getDoubleExtra("SELECTED_LATITUDE", -1.0)
            val selectedLongitude = data?.getDoubleExtra("SELECTED_LONGITUDE", -1.0)
            val selectedAddress = data?.getStringExtra("SELECTED_ADDRESS")

            if (selectedLatitude != null && selectedLongitude != null) {
                val selectedLocation = LatLng(selectedLatitude, selectedLongitude)
                if (requestCode == REQUEST_CODE_SEARCH_FROM) {
                    fromLocation = selectedLocation
                    fromMarker?.remove()
                    fromMarker = map.addMarker(MarkerOptions().position(selectedLocation).title("From: $selectedAddress"))
                    UserRepository.userFromLocation = selectedLocation
                } else if (requestCode == REQUEST_CODE_SEARCH_TO) {
                    toLocation = selectedLocation
                    toMarker?.remove()
                    toMarker = map.addMarker(MarkerOptions().position(selectedLocation).title("To: $selectedAddress"))
                    UserRepository.userToLocation = selectedLocation
                }
                // Ajustar la cámara para mostrar ambos marcadores
                val builder = LatLngBounds.Builder()
                fromLocation?.let { builder.include(it) }
                toLocation?.let { builder.include(it) }
                val bounds = builder.build()
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
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
                headerPlace()
            } else {
                Toast.makeText(this, "Acepte los permisos de localización", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, "Ésta es tu ubicación actual", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun drawBackendRoute(routeResponse: BackendRouteResponse) {
        val polylineOptions = PolylineOptions()
        routeResponse.geojson.features.firstOrNull()?.geometry?.coordinates?.forEach { coordinate ->
            polylineOptions.add(LatLng(coordinate[1], coordinate[0]))
        }
        runOnUiThread {
            currentPolyline?.remove()
            currentPolyline = map.addPolyline(polylineOptions)
            currentPolyline?.color = ContextCompat.getColor(this, R.color.btnColor)
            currentPolyline?.width = 12f
            currentPolyline?.endCap = CustomCap(resizeIcon(R.drawable.bus, this, 50, 50))
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = LatLng(location.latitude, location.longitude)
                        val coordinates = routeResponse.geojson.features.firstOrNull()?.geometry?.coordinates
                        if (coordinates != null) {
                            val closestPoint = findClosestPointOnPolyline(userLocation, coordinates)
                            currentMarker?.remove()
                            currentMarker = map.addMarker(
                                MarkerOptions()
                                    .position(closestPoint)
                                    .title("Parada más cercana")
                                    .icon(resizeIcon(R.drawable.bus_stop, this, 100, 100))
                            )
                            createRoute(userLocation, closestPoint)
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
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private fun drawRoute(routeResponse: RouteResponse?) {
        val polylineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polylineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            val poly = map.addPolyline(polylineOptions)
            poly.color = ContextCompat.getColor(this, R.color.routeMap)
            poly.pattern = listOf(Dot(), Gap(10f))
        }
    }
    private fun createRoute(start: LatLng, end: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java)
                .getRouteApiService(
                    "5b3ce3597851110001cf6248e6564815347a41768ab3ab3cf1098048",
                    "${start.longitude},${start.latitude}",
                    "${end.longitude},${end.latitude}"
                )
            if (call.isSuccessful) {
                drawRoute(call.body())
                Log.i("alfredoDev", "OK")
            } else {
                Log.i("alfredoDev", "NOT OK")
            }
        }
    }
}