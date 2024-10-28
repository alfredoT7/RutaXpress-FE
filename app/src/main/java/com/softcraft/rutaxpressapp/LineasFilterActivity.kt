package com.softcraft.rutaxpressapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.softcraft.rutaxpressapp.lineas.LineaResponse
import com.softcraft.rutaxpressapp.lineas.LineasAdapter
import com.softcraft.rutaxpressapp.lineas.LineasRepository
import com.softcraft.rutaxpressapp.routes.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LineasFilterActivity : AppCompatActivity() {
    private lateinit var rvLineas: RecyclerView
    private lateinit var lineasAdapter: LineasAdapter
    private var allLineas: List<LineaResponse> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_lineas_filter)
        initComponents()
        if (LineasRepository.lineas == null){
            obtenerLineas()
        }else{
            allLineas = LineasRepository.lineas!!
            lineasAdapter.submitList(allLineas)
        }
        initListeners()
    }
    private fun initComponents() {
        rvLineas = findViewById(R.id.rvLineas)
        rvLineas.layoutManager = LinearLayoutManager(this)
        lineasAdapter = LineasAdapter { linea -> onLineaClick(linea) }
        rvLineas.adapter = lineasAdapter
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
                        allLineas = lineas
                        LineasRepository.lineas = lineas
                        runOnUiThread {
                            lineasAdapter.submitList(lineas)
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
        val etSearchId: EditText = findViewById(R.id.etSearchId)
        etSearchId.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val id = etSearchId.text.toString()
                if (id.isNotEmpty()) {
                    filtrarLineas(id)
                }
                true
            } else {
                false
            }
        }
        etSearchId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    lineasAdapter.submitList(allLineas)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun filtrarLineas(id: String) {
        val lineasFiltradas = allLineas.filter { it.routeId.contains(id, ignoreCase = true) }
        runOnUiThread {
            lineasAdapter.submitList(lineasFiltradas)
        }
    }
    private fun onLineaClick(linea: LineaResponse) {
        val intent = Intent(this, InitialMapActivity::class.java)
        intent.putExtra("routeId", linea.routeId)
        startActivity(intent)
    }
}