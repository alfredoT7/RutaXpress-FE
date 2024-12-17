package com.softcraft.rutaxpressapp.viewsDriver

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import com.softcraft.rutaxpressapp.LoginActivity
import com.softcraft.rutaxpressapp.R
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import com.softcraft.rutaxpressapp.service.ApiClient
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale


class InitialDriverActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var socket: Socket
    private lateinit var map: GoogleMap
    private lateinit var tvCurrentPlace: TextView
    private lateinit var tvUserName: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tvDriverLine: TextView
    private lateinit var tvRouteDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_driver)
        tvCurrentPlace = findViewById(R.id.tvCurrentPlace)
        tvUserName = findViewById(R.id.tvUserName)
        imgProfile = findViewById(R.id.imgProfile)
        tvDriverLine = findViewById(R.id.tvDriverLine)
        tvRouteDescription = findViewById(R.id.tvRouteDescription)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        socket = IO.socket("http://localhost:3000")
        socket.connect()
        socket.on("receiveLocation") { args ->
            if (args.isNotEmpty()) {
                val location = args[0] as JsonObject
                val latitude = location["latitude"].asDouble
                val longitude = location["longitude"].asDouble
                runOnUiThread {
                    Toast.makeText(this, "Nueva ubicación: $latitude, $longitude", Toast.LENGTH_SHORT).show()
                    val driverLocation = LatLng(latitude, longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLng(driverLocation))
                }
            }
        }

        loadUserProfile()
        createFragment()
        fetchDriverRouteAndDescription()
    }

    private fun sendLocationToPassenger(latitude: Double, longitude: Double) {
        val location = JSONObject()
        location.put("latitude", latitude)
        location.put("longitude", longitude)
        socket.emit("sendLocation", location)
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val username = document.getString("username") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    tvUserName.text = username
                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(this).load(profileImageUrl)
                            .circleCrop()
                            .into(imgProfile)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (isLocationPermissionGranted()) {
            enableLocation()
            startUpdatingLocation()
            moveCameraToCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun startUpdatingLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val handler = android.os.Handler(mainLooper)
        val updateInterval = 5000L // Intervalo de actualización en milisegundos (5 segundos)

        val updateLocationTask = object : Runnable {
            override fun run() {
                if (ActivityCompat.checkSelfPermission(
                        this@InitialDriverActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = LatLng(location.latitude, location.longitude)

                        // Mueve la cámara si es necesario
                        map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))

                        // Actualiza la vista de la dirección
                        updateCurrentPlaceTextView(userLocation)

                        // Envía la ubicación a los pasajeros
                        sendLocationToPassenger(location.latitude, location.longitude)
                    }
                }
                // Vuelve a ejecutar la tarea después del intervalo
                handler.postDelayed(this, updateInterval)
            }
        }
        // Ejecuta la primera tarea
        handler.post(updateLocationTask)
    }

    private fun moveCameraToCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                updateCurrentPlaceTextView(userLocation)
                sendLocationToPassenger(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCurrentPlaceTextView(userLocation: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val address: Address = addresses[0]
                val addressText: String = address.getAddressLine(0)
                val shortAddress = addressText.split(",")[0]
                tvCurrentPlace.text = shortAddress
            } else {
                tvCurrentPlace.text = "Dirección no disponible"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            tvCurrentPlace.text = "Error al obtener la dirección"
        }
    }

    private fun isLocationPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation()
                startUpdatingLocation()
                moveCameraToCurrentLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_LOCATION = 0
    }

    fun openProfileMenu(view: View){
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_view_profile -> {
                    showUserProfile()
                    true
                }
                R.id.action_settings -> {
                    openSettings()
                    true
                }
                R.id.action_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }


    private fun logoutUser() {
        val shardedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(shardedPref.edit()){
            clear()
            apply()
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openSettings() {
        Toast.makeText(this, "Configuraciones no implementadas aún", Toast.LENGTH_SHORT).show()
    }


    private fun showUserProfile() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("userName", "Usuario")
        val useEmail = sharedPref.getString("userEmail", "Correo no disponible")

        AlertDialog.Builder(this)
            .setTitle("Perfil de usuario")
            .setMessage("Nombre: $userName\nCorreo: $useEmail")
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss()}
            .show()
    }

    private fun fetchDriverRouteAndDescription() {
        val userId = auth.currentUser?.uid ?: return
        val apiService = ApiClient.apiService

        lifecycleScope.launch {
            try {
                val driverRouteResponse = apiService.getDriverRoute(userId)
                if (driverRouteResponse.isSuccessful) {
                    val driverRoute = driverRouteResponse.body()
                    driverRoute?.routes?.firstOrNull()?.let { routeId ->
                        tvDriverLine.text = "Línea: $routeId"
                        fetchRouteDescription(routeId)
                    }
                } else {
                    Log.e(TAG, "Error al obtener la ruta del conductor: ${driverRouteResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al realizar la petición: ${e.message}")
            }
        }
    }
    private suspend fun fetchRouteDescription(routeId: String) {
        val apiService = ApiClient.apiService
        try {
            val routeDescriptionResponse = apiService.getRouteDescription(routeId)
            if (routeDescriptionResponse.isSuccessful) {
                val routeDescription = routeDescriptionResponse.body()
                tvRouteDescription.text = "Ruta: ${routeDescription?.description}"
            } else {
                Log.e(TAG, "Error al obtener la descripción de la ruta: ${routeDescriptionResponse.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al realizar la petición: ${e.message}")
        }
    }
}