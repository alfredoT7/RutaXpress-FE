package com.softcraft.rutaxpressapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class SearchActivity : AppCompatActivity() {
    //variable del autocompletado
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_screen)

        Places.initialize(applicationContext,getString(R.string.google_maps_key))
        // Configura el fragmento de autocompletado
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                // Aquí puedes manejar la selección de lugar
            }

            override fun onError(status: Status) {
                Toast.makeText(this@SearchActivity, "Algo ha fallado al buscar", Toast.LENGTH_SHORT).show()
            }
        })
        // Configurar la flecha de regreso
        val backArrowIcon: ImageView = findViewById(R.id.back_arrow_icon)
        backArrowIcon.setOnClickListener {
            finish()  // Finaliza la actividad actual para regresar
        }
    }
}
