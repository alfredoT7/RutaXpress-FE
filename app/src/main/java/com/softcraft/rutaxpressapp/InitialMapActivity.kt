package com.softcraft.rutaxpressapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class InitialMapActivity : AppCompatActivity(), OnMapReadyCallback {

    // Variable para el mapa
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_map)
        createFragment()
    }

    private fun createFragment() {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) //implementamos OnReadyCallback
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
}
