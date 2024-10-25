package com.softcraft.rutaxpressapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.softcraft.rutaxpressapp.lineas.LineasAdapter
import com.softcraft.rutaxpressapp.routes.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LineasFilterActivity : AppCompatActivity() {
    private lateinit var rvLineas: RecyclerView

    private lateinit var lineasAdapter: LineasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_lineas_filter)
        initComponents()
        obtenerLineas()
        initListeners()
    }

    private fun initComponents() {
        rvLineas = findViewById(R.id.rvLineas)
        rvLineas.layoutManager = LinearLayoutManager(this)
    }
    private fun obtenerLineas() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://ruta-xpress-backend-express-js.vercel.app/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiService = retrofit.create(ApiService::class.java)
                val response = apiService.getLineas()
                if (response.isSuccessful) {
                    response.body()?.let { lineas ->
                        runOnUiThread {
                            lineasAdapter = LineasAdapter(lineas)
                            rvLineas.adapter = lineasAdapter
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LineasFilterActivity, "Error al obtener las líneas: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                    Log.e("LineasFilterActivity", "Error al obtener las líneas: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@LineasFilterActivity, "Excepción al obtener las líneas: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("LineasFilterActivity", "Excepción al obtener las líneas: ${e.message}")
            }
        }
    }


    private fun initListeners() {
    }


}