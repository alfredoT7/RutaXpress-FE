package com.softcraft.rutaxpressapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val driverButton: Button = findViewById(R.id.driverButton)
        val passengerButton: Button = findViewById(R.id.passengerButton)

        driverButton.setOnClickListener {
            navigateToRegisterActivity("Conductor")
        }

        passengerButton.setOnClickListener {
            navigateToRegisterActivity("Pasajero")
        }
    }

    private fun navigateToRegisterActivity(role: String) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("USER_ROLE", role) // Enviar el rol seleccionado
        startActivity(intent)
    }
}
