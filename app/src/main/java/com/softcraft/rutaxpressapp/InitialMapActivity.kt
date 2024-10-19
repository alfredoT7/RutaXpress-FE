package com.softcraft.rutaxpressapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.gms.maps.model.PolylineOptions
import com.softcraft.rutaxpressapp.routes.ApiService
import com.softcraft.rutaxpressapp.routes.RouteResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class InitialMapActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener {

    // Variables globales
    private lateinit var map: GoogleMap
    private var start:String = "-66.17131236994032,-17.379954096571343"//TENEMOS QUE CREAR UNA BASE DE DATOS CON CORRDENADAS DE LAS PARADAS de los trugis
    private var end:String = "-66.14519448429931,-17.392252179261888"//HACEMOS UN BACK FACIL PERO TENEMOS QUE HACERLO, DESPUES PINTAR ESTAS RUTAS ES FACIL

    companion object{
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_map)

        // Solicitar permisos antes de crear el mapa
        if (isLocationPermissionGranted()) {
            createFragment()
            createRoute()
        } else {
            requestLocationPermission()  // Esto debería solicitar los permisos
        }

    }

    fun showExpandedSearch(view: View) {
        // Ocultar el EditText original
        val originalSearchEditText = findViewById<EditText>(R.id.original_search_edit_text)
        originalSearchEditText.visibility = View.GONE

        // Mostrar el layout expandido
        val expandedSearchLayout = findViewById<FrameLayout>(R.id.expanded_search_layout)
        expandedSearchLayout.visibility = View.VISIBLE

        // Enfocar el nuevo EditText en el layout expandido
        val expandedSearchEditText = findViewById<EditText>(R.id.expanded_search_edit_text)
        expandedSearchEditText.requestFocus()
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

    private fun createRoute() {
        CoroutineScope(Dispatchers.IO).launch{
            val request = getRetrofit().create(ApiService::class.java)
                .getRoute(
                    "",//Aqui va la API Key, me la piden por privado HIJAS
                    start,
                    end
                )
            if (request.isSuccessful){
                drawRoute(request.body())
                Log.i("alfredoDev","OK")
            }else{
                Log.i("alfredoDev","NOT OK")
            }
        }
    }

    /**
     * Hay QUE REFACTORIZAR HIJAS mejor, talque no se haga todo en un archivos, para no afectar la funcionalidad creo como REACT
     */

    private fun drawRoute(routeResponse: RouteResponse?) {
        val polylineOptions = PolylineOptions()
        routeResponse?.features?.get(0)?.geometry?.coordinates?.forEach {
            polylineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread{
            val poly = map.addPolyline(polylineOptions)
            poly.color = ContextCompat.getColor(this, R.color.routeMap)
            poly.width = 12f
            poly.endCap = CustomCap(resizeIcon(R.drawable.bus, this))
        }
    }
    private fun resizeIcon(resourceId: Int, context: Context): BitmapDescriptor {
        val imageBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 50, 50, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}
