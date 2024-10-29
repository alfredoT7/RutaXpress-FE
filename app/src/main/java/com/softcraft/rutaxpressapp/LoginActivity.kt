package com.softcraft.rutaxpressapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initComponents()
        initListeners()
    }

    private fun initComponents() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun initListeners() {
        btnLogin.setOnClickListener {
            val email = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Llamamos al método que verifica las credenciales con Firebase
                loginUserWithFirebase(email, password)
            } else {
                Toast.makeText(this, "Por favor ingrese todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUserWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso
                    Log.d("LoginActivity", "Login con éxito: ${auth.currentUser?.email}")
                    val intent = Intent(this, InitialMapActivity::class.java)
                    startActivity(intent)
                    finish() // Para evitar regresar a la pantalla de login al presionar atrás
                } else {
                    // Error en el login
                    Log.e("LoginActivity", "Error en login: ${task.exception?.message}")
                    Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
