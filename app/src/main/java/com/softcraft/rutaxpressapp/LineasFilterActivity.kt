package com.softcraft.rutaxpressapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.softcraft.rutaxpressapp.lineas.LineasAdapter

class LineasFilterActivity : AppCompatActivity() {
    private lateinit var rvLineas: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_lineas_filter)
        initComponents()
        initUI()
        initListeners()
    }

    private fun initComponents() {
        rvLineas = findViewById(R.id.rvLineas)
    }

    private fun initUI() {
        rvLineas.layoutManager = LinearLayoutManager(this)
        rvLineas.adapter = LineasAdapter()
    }

    private fun initListeners() {
        TODO("Not yet implemented")
    }


}