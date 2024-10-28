package com.softcraft.rutaxpressapp

import android.content.Intent
import android.os.Bundle
import android.text.BoringLayout
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername:EditText
    private lateinit var etPassword:EditText
    private lateinit var btnLogin:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        initComponents()
        initListeners()
    }
    private fun initComponents() {

        //etUsername = findViewById(R.id.etUsername)
        //etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }
    private fun initListeners() {
        btnLogin.setOnClickListener {
//            val username = etUsername.text.toString()
//            val password = etPassword.text.toString()
//            if (username.isNotEmpty() && password.isNotEmpty()) {
//                requestLogin()
//            }
            val att: Boolean = true
            requestLogin(att)
        }
    }

    private fun requestLogin(att:Boolean) {
        Log.i("RutaXpress", "Login request boton DONE")

        //Test only map
        if(att){
            val attemp = Intent(this, InitialMapActivity::class.java)
            startActivity(attemp)
        }else{
            Log.i("RutaXpress", "No es posible logearse")
        }
    }
}