    package com.softcraft.rutaxpressapp

    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.widget.Button
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.app.AppCompatDelegate
    import com.google.android.material.textfield.TextInputEditText
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.ktx.auth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.ktx.Firebase
    import com.softcraft.rutaxpressapp.user.UserRepository

    data class UserProfile(
        val username: String = "",
        val lastName: String = "",
        val email: String = "",
        val phone: String = "",
        val birthDate: String = "",
        val profileImageUrl: String? = null
    )

    class LoginActivity : AppCompatActivity() {
        private lateinit var auth: FirebaseAuth
        private lateinit var db: FirebaseFirestore

        private lateinit var etUsername: TextInputEditText
        private lateinit var etPassword: TextInputEditText
        private lateinit var btnLogin: Button
        private lateinit var tvRegister: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            auth = Firebase.auth
            db = FirebaseFirestore.getInstance()  // Inicializar Firestore

            if (isUserLoggedIn()) {
                loadUserDataAndNavigate()
            } else {
                setContentView(R.layout.activity_login)
                initComponents()
                initListeners()

            }
        }

        private fun isUserLoggedIn(): Boolean {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            return sharedPref.getString("userId", null) != null
        }

        private fun initComponents() {
            etUsername = findViewById(R.id.etUsername)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            tvRegister = findViewById(R.id.tvRegister)
        }

        private fun initListeners() {
            btnLogin.setOnClickListener {
                val email = etUsername.text.toString()
                val password = etPassword.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUserWithFirebase(email, password)
                } else {
                    Toast.makeText(this, "Por favor ingrese todos los campos", Toast.LENGTH_SHORT).show()
                }
            }

            tvRegister.setOnClickListener {
                //val intent = Intent(this, RegisterActivity::class.java)
                //startActivity(intent)
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }

        private fun loginUserWithFirebase(email: String, password: String) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""
                        val token = auth.currentUser?.getIdToken(false)?.result?.token ?: ""

                        saveUserId(userId)
                        saveToken(token)

                        Log.d("LoginActivity", "Login con éxito: ${auth.currentUser?.email}")
                        loadUserDataAndNavigate()
                    } else {
                        Log.e("LoginActivity", "Error en login: ${task.exception?.message}")
                        Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        private fun saveToken(token: String) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("token", token)
                apply()
            }
        }

        private fun loadUserDataAndNavigate() {
            val userId = auth.currentUser?.uid ?: return
            saveUserId(userId)
            UserRepository.userId = userId
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userProfile = UserProfile(
                            username = document.getString("username") ?: "",
                            lastName = document.getString("lastName") ?: "",
                            email = document.getString("email") ?: "",
                            phone = document.getString("phone") ?: "",
                            birthDate = document.getString("birthDate") ?: "",
                            profileImageUrl = document.getString("profileImageUrl")
                        )

                        saveUserProfile(userProfile)

                        // Navegar a InitialMapActivity solo después de cargar los datos
                        val intent = Intent(this, InitialMapActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun saveUserId(userId: String) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("userId", userId)
                apply()
            }
        }

        private fun saveUserProfile(userProfile: UserProfile) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("username", userProfile.username)
                putString("lastName", userProfile.lastName)
                putString("email", userProfile.email)
                putString("phone", userProfile.phone)
                putString("birthDate", userProfile.birthDate)
                putString("profileImageUrl", userProfile.profileImageUrl)
                apply()
            }
        }
    }
