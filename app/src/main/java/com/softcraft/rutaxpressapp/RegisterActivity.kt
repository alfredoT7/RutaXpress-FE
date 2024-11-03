package com.softcraft.rutaxpressapp

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
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
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        initCloudinary()
        initComponents()
        initListeners()
    }

    private fun initCloudinary() {
        val config = mapOf(
            "cloud_name" to "djcfm4nd2",
            "api_key" to "897657815927312",
            "api_secret" to "6Af5mOu8kiKfn9MT-P3Ag6vXF1s"
        )
        MediaManager.init(this, config)
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

        etBirthDate.setOnClickListener { showDatePickerDialog() }

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            profileImage.setImageURI(selectedImageUri)
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

        return true
    }

    private fun registerUserWithFirebase(email: String, password: String, username: String, lastName: String, birthDate: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                uploadProfileImage(userId, email, username, lastName, birthDate, phone)
            } else {
                Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProfileImage(userId: String, email: String, username: String, lastName: String, birthDate: String, phone: String) {
        if (selectedImageUri != null) {
            MediaManager.get().upload(selectedImageUri)
                .option("public_id", "rutaXpess/$userId") // Cambia la ruta a "rutaXpess/$userId"
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val profileImageUrl = resultData["secure_url"] as String
                        saveUserToFirestore(userId, email, username, lastName, birthDate, phone, profileImageUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(this@RegisterActivity, "Error al subir la imagen: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        } else {
            saveUserToFirestore(userId, email, username, lastName, birthDate, phone, null)
        }
    }


    private fun saveUserToFirestore(userId: String, email: String, username: String, lastName: String, birthDate: String, phone: String, profileImageUrl: String?) {
        val user = hashMapOf(
            "username" to username,
            "lastName" to lastName,
            "birthDate" to birthDate,
            "email" to email,
            "phone" to phone,
            "profileImageUrl" to profileImageUrl
        )

        FirebaseFirestore.getInstance().collection("users").document(userId).set(user).addOnSuccessListener {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
