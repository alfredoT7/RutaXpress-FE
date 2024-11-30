package com.softcraft.rutaxpressapp

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.softcraft.rutaxpressapp.DriverView.RegisterDriverActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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

        val userRole = intent.getStringExtra("USER_ROLE") ?: "No especificado"
        if (savedInstanceState == null) {
            Toast.makeText(this, "Rol seleccionado: $userRole", Toast.LENGTH_SHORT).show()
        }
        if (userRole.equals("Conductor")) {
            registerDriver()
        }
        initCloudinary()
        initComponents()
        initListeners()
        Toast.makeText(this, "Rol seleccionado: $userRole", Toast.LENGTH_SHORT).show()
    }

    private fun registerDriver() {

    }

    /**
     * Inicializa Cloudinary con las configuraciones necesarias.
     */
    private fun initCloudinary() {
        val config = mapOf(
            "cloud_name" to "djcfm4nd2",
            "api_key" to "897657815927312",
            "api_secret" to "6Af5mOu8kiKfn9MT-P3Ag6vXF1s"
        )
        MediaManager.init(this, config)
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
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

    /**
     * Configura los listeners de eventos para los elementos de la UI.
     */
    private fun initListeners() {
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val birthDate = etBirthDate.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (validateInputs(
                    email,
                    password,
                    confirmPassword,
                    username,
                    lastName,
                    birthDate,
                    phone
                )
            ) {
                registerUserWithFirebase(email, password, username, lastName, birthDate, phone)
            }
        }

        etBirthDate.setOnClickListener { showDatePickerDialog() }

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1000)
        }
    }

    /**
     * Maneja la selección de la imagen de perfil del usuario.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            Glide.with(this)
                .load(selectedImageUri)
                .placeholder(R.drawable.ic_default_profile)
                .into(profileImage)
        }
    }

    /**
     * Muestra el DatePicker para seleccionar la fecha de nacimiento.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                etBirthDate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)
        datePickerDialog.show()
    }

    /**
     * Valida los campos de entrada del formulario de registro.
     */
    private fun validateInputs(
        email: String,
        password: String,
        confirmPassword: String,
        username: String,
        lastName: String,
        birthDate: String,
        phone: String
    ): Boolean {
        val errorMessages = mutableListOf<String>()
        if (username.isEmpty()) errorMessages.add("El nombre de usuario está vacío")
        if (lastName.isEmpty()) errorMessages.add("El apellido está vacío")
        if (email.isEmpty()) errorMessages.add("El correo electrónico está vacío")
        if (password.isEmpty() || confirmPassword.isEmpty()) errorMessages.add("La contraseña está vacía")
        if (password != confirmPassword) errorMessages.add("Las contraseñas no coinciden")
        if (birthDate.isEmpty()) errorMessages.add("La fecha de nacimiento está vacía")
        if (phone.isEmpty()) errorMessages.add("El teléfono está vacío")

        return if (errorMessages.isNotEmpty()) {
            Toast.makeText(this, errorMessages.joinToString("\n"), Toast.LENGTH_LONG).show()
            false
        } else {
            true
        }
    }

    /**
     * Registra un usuario en Firebase Authentication.
     */
    private fun registerUserWithFirebase(
        email: String,
        password: String,
        username: String,
        lastName: String,
        birthDate: String,
        phone: String
    ) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                }
                val userId = auth.currentUser?.uid ?: return@launch
                uploadProfileImage(userId, email, username, lastName, birthDate, phone)
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Error al registrar usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Comprime la imagen seleccionada antes de subirla.
     */
    private fun compressImage(uri: Uri): ByteArray {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Sube la imagen de perfil del usuario a Cloudinary.
     */
    private fun uploadProfileImage(
        userId: String,
        email: String,
        username: String,
        lastName: String,
        birthDate: String,
        phone: String
    ) {
        if (selectedImageUri != null) {
            val compressedImage = compressImage(selectedImageUri!!)
            MediaManager.get().upload(compressedImage)
                .option("public_id", "profileImages/$userId")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val profileImageUrl = resultData["secure_url"] as String
                        saveUserToFirestore(
                            userId,
                            email,
                            username,
                            lastName,
                            birthDate,
                            phone,
                            profileImageUrl
                        )
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Error al subir la imagen: ${error.description}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        } else {
            saveUserToFirestore(userId, email, username, lastName, birthDate, phone, null)
        }
    }

    /**
     * Guarda la información del usuario en Firebase Firestore.
     */
    private fun saveUserToFirestore(
        userId: String,
        email: String,
        username: String,
        lastName: String,
        birthDate: String,
        phone: String,
        profileImageUrl: String?
    ) {
        val userRole = intent.getStringExtra("USER_ROLE") ?: "No especificado"

        val user = hashMapOf(
            "username" to username,
            "lastName" to lastName,
            "birthDate" to birthDate,
            "email" to email,
            "phone" to phone,
            "profileImageUrl" to profileImageUrl,
            "role" to userRole
        )

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.set(user).addOnSuccessListener {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            if (userRole == "Conductor") {
                saveDriverToFirestore(
                    userId,
                    username,
                    lastName,
                    email,
                    phone,
                    birthDate,
                    profileImageUrl
                )
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDriverToFirestore(
        userId: String,
        username: String,
        lastName: String,
        email: String,
        phone: String,
        birthDate: String,
        profileImageUrl: String?
    ) {
        val driver = hashMapOf(
            "username" to username,
            "lastName" to lastName,
            "email" to email,
            "phone" to phone,
            "birthDate" to birthDate,
            "profileImageUrl" to profileImageUrl
        )

        val db = FirebaseFirestore.getInstance()
        val driverRef = db.collection("drivers").document(userId)

        driverRef.set(driver).addOnSuccessListener {
            Toast.makeText(this, "Registro de conductor exitoso", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, RegisterDriverActivity::class.java)
            intent.putExtra("USER_ID", userId)
            Toast.makeText(this, "$userId", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this,
                "Error al guardar datos del conductor: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
