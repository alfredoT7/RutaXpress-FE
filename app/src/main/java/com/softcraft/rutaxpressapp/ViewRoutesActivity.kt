package com.softcraft.rutaxpressapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ViewRoutesActivity : AppCompatActivity() {
    val routeId = intent.getStringExtra("routeId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_routes)
        initComponents()
        initListeners()
    }
    private fun initComponents() {

    }
    private fun initListeners() {

    }
}