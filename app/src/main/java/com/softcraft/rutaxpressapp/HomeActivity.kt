package com.softcraft.rutaxpressapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si el usuario está logeado al abrir la app
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // Si está logeado, navegar a
            val intent = Intent(this, InitialMapActivity::class.java)
            startActivity(intent)
            finish() // Finalizar HomeActivity para evitar volver aquí con el botón "Atrás"
            return
        }

        setContentView(R.layout.activity_home)

        // Inicializar botones
        val driverButton: Button = findViewById(R.id.driverButton)
        val passengerButton: Button = findViewById(R.id.passengerButton)
//        val loginButton: Button = findViewById(R.id.loginButton)
//        val registerButton: Button = findViewById(R.id.registerButton)

//         Configurar listeners
        driverButton.setOnClickListener {
            navigateToRegisterActivity("Conductor")
        }

        passengerButton.setOnClickListener {
            navigateToRegisterActivity("Pasajero")
        }

//        loginButton.setOnClickListener {
//            navigateToLoginActivity()
//        }
//
//        registerButton.setOnClickListener {
//            navigateToRegisterActivity(null)
//        }
    }

    private fun navigateToRegisterActivity(role: String?) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("USER_ROLE", role)
        startActivity(intent)
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
