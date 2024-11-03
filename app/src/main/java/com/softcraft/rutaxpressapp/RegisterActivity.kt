package com.softcraft.rutaxpressapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import android.app.DatePickerDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var etUsername: EditText
    private lateinit var etLastName: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var profileImage: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth

        initComponents()
        initListeners()
    }

    private fun initComponents() {
        etUsername = findViewById(R.id.etUsername)
        etLastName = findViewById(R.id.etLastName)
        etBirthDate = findViewById(R.id.etBirthDate)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        profileImage = findViewById(R.id.profileImage)
    }

    private fun initListeners() {
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val birthDate = etBirthDate.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (validateInputs(email, password, confirmPassword, username, lastName, birthDate, phone)) {
                registerUserWithFirebase(email, password, username, lastName, birthDate, phone)
            }
        }

        etBirthDate.setOnClickListener {
            showDatePickerDialog()
        }

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            etBirthDate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String, username: String, lastName: String, birthDate: String, phone: String): Boolean {
        if (username.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || birthDate.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar que la edad sea al menos 13 años
        val calendar = Calendar.getInstance()
        val birthYear = birthDate.split("/")[2].toInt()
        val currentYear = calendar.get(Calendar.YEAR)
        if (currentYear - birthYear < 13) {
            Toast.makeText(this, "Debes tener al menos 13 años", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUserWithFirebase(email: String, password: String, username: String, lastName: String, birthDate: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "username" to username,
                        "lastName" to lastName,
                        "birthDate" to birthDate,
                        "email" to email,
                        "phone" to phone
                    )

                    // Guardar en Firestore
                    val db = FirebaseFirestore.getInstance()
                    userId?.let {
                        db.collection("users").document(it)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "Datos adicionales guardados en Firestore")
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Imprimir el error en la consola con más contexto
                                Log.e("RegisterActivity", "Error al guardar datos en Firestore: ${e.message}", e)
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Log.e("RegisterActivity", "Error en registro: ${task.exception?.message}", task.exception)
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data  //  URI de la imagen seleccionada
            try {
                // Muestra la imagen seleccionada en el ImageView
                profileImage.setImageURI(imageUri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
