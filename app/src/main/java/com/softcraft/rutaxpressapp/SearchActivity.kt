package com.softcraft.rutaxpressapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var userLocation: LatLng
    private lateinit var confirmButton: Button
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.search_screen)

        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        // Configurar el fragmento de autocompletado
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        )

        val latitude = intent.getDoubleExtra("LATITUDE", -34.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 151.0)
        userLocation = LatLng(latitude,longitude)

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                // Mueve el mapa al lugar seleccionado y añade un marcador
                if (latLng != null) {
                    selectedLatLng = latLng
                    selectedAddress = place.address

                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(latLng).title("Ubicación Seleccionada"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                    confirmButton.isEnabled = true
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(this@SearchActivity, "Algo ha fallado al buscar", Toast.LENGTH_SHORT).show()
            }
        })

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapSearch) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar el botón de confirmación
        confirmButton = findViewById(R.id.confirm_button)
        confirmButton.setOnClickListener {
            selectedLatLng?.let { location ->
                val returnIntent = Intent()
                returnIntent.putExtra("SELECTED_LATITUDE", location.latitude)
                returnIntent.putExtra("SELECTED_LONGITUDE", location.longitude)
                returnIntent.putExtra("SELECTED_ADDRESS", selectedAddress)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        // Centra el mapa en la ubicación del usuario recibida
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

        try{
            enableLocation()
        }catch(Any: Exception){
            Toast.makeText(this@SearchActivity, "Error en mostrar ubicacion actual", Toast.LENGTH_SHORT).show()
        }

        // Detectamos toques en el mapa para colocar un marker
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            selectedLatLng = latLng
            confirmButton.isEnabled = true
        }
    }

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }
    }
}