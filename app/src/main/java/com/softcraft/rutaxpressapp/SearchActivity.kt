package com.softcraft.rutaxpressapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var googleMap: GoogleMap  // Variable para el Google Map

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_screen)

        // Inicializar Places API
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        // Configurar el fragmento de autocompletado
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                // Mueve el mapa al lugar seleccionado y añade un marcador
                if (latLng != null) {
                    googleMap.clear()  // Limpia marcadores anteriores
                    googleMap.addMarker(MarkerOptions().position(latLng).title(place.address))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(this@SearchActivity, "Algo ha fallado al buscar", Toast.LENGTH_SHORT).show()
            }
        })

        // Configurar el fragmento de mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapSearch) as SupportMapFragment
        mapFragment.getMapAsync(this)  // Obtén el mapa de forma asíncrona

        // Configurar la flecha de regreso
//        val backArrowIcon: ImageView = findViewById(R.id.back_arrow_icon)
//        backArrowIcon.setOnClickListener {
//            finish()  // Finaliza la actividad actual para regresar
//        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map  // Asigna el mapa cuando esté listo
        googleMap.uiSettings.isZoomControlsEnabled = true  // Habilita controles de zoom, opcional
    }
}

