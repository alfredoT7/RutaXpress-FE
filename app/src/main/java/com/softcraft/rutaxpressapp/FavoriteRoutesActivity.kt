package com.softcraft.rutaxpressapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.softcraft.rutaxpressapp.lineas.LineaResponse
import com.softcraft.rutaxpressapp.lineas.LineasAdapter
import com.softcraft.rutaxpressapp.routes.ApiService
import com.softcraft.rutaxpressapp.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavoriteRoutesActivity : AppCompatActivity() {
    private lateinit var rvLineas: RecyclerView
    private lateinit var lineasAdapter: LineasAdapter
    private lateinit var etSearchId:EditText
    private var favoriteRoutes: List<LineaResponse> = emptyList()
    private var allFavoriteRoutes: List<LineaResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_routes)
        initComponents()
        obtenerRutasFavoritas()
        initListeners()
    }
    private fun initComponents() {
        rvLineas = findViewById(R.id.rvLineas)
        rvLineas.layoutManager = LinearLayoutManager(this)
        lineasAdapter = LineasAdapter { linea -> onLineaClick(linea) }
        rvLineas.adapter = lineasAdapter
        etSearchId = findViewById(R.id.etSearchId)
    }
    private fun obtenerRutasFavoritas() {
        val userId = UserRepository.userId
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://ruta-xpress-backend-express-js.vercel.app/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiService = retrofit.create(ApiService::class.java)
                val response = apiService.getFavoriteRoutes(userId!!)
                if (response.isSuccessful) {
                    response.body()?.let { favoriteRoutesResponse ->
                        allFavoriteRoutes = favoriteRoutesResponse.favoriteRoutes
                        favoriteRoutes = allFavoriteRoutes
                        runOnUiThread {
                            lineasAdapter.submitList(favoriteRoutes)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@FavoriteRoutesActivity, "Error al obtener las rutas favoritas: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@FavoriteRoutesActivity, "Excepción al obtener las rutas favoritas: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun onLineaClick(linea: LineaResponse) {
        val intent = Intent(this, ViewRoutesActivity::class.java)
        intent.putExtra("routeId", linea.routeId)
        startActivity(intent)
    }
    private fun initListeners() {
        etSearchId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    lineasAdapter.submitList(allFavoriteRoutes)
                } else {
                    filtrarRutas(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun filtrarRutas(query: String) {
        val rutasFiltradas = allFavoriteRoutes.filter { it.routeId.contains(query, ignoreCase = true) }
        runOnUiThread {
            lineasAdapter.submitList(rutasFiltradas)
        }
    }
}