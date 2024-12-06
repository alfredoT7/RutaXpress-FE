package com.softcraft.rutaxpressapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this, InitialMapActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        // Inicializar botones y CheckBox
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerButton: Button = findViewById(R.id.registerButton)
        val acceptPoliciesCheckBox: CheckBox = findViewById(R.id.acceptPoliciesCheckBox)

        // Listener para el CheckBox
        acceptPoliciesCheckBox.setOnCheckedChangeListener { _, isChecked ->
            registerButton.isEnabled = isChecked
        }

        loginButton.setOnClickListener {
            navigateToLoginActivity()
        }

        registerButton.setOnClickListener {
            if (acceptPoliciesCheckBox.isChecked) {
                navigateToRegisterActivity()
            } else {
                Toast.makeText(this, "Debes aceptar las políticas y condiciones.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
