package com.softcraft.rutaxpressapp.viewsDriver

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.softcraft.rutaxpressapp.LoginActivity
import com.softcraft.rutaxpressapp.R
import okhttp3.*
import java.io.File
import java.io.IOException
import java.io.InputStream

class RegisterDriverActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE_1 = 1
        private const val REQUEST_IMAGE_CAPTURE_2 = 2
        private const val REQUEST_IMAGE_CAPTURE_3 = 3
        private const val REQUEST_IMAGE_CAPTURE_4 = 4
        private const val REQUEST_VEHICLE_FORM = 5
        private const val TAG = "RegisterDriverActivity"
        private const val PERMISSION_REQUEST_CODE = 100
    }
    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null
    private var imageUri3: Uri? = null
    private var imageUri4: Uri? = null
    private var imageUriVehicle: Uri? = null
    private var imageUriRUAT: Uri? = null
    private lateinit var cvCarnetDeIdentidad: CardView
    private lateinit var cvDriverLicense: CardView
    private lateinit var cvDataVehicle: CardView
    private lateinit var btnFinishRegister: Button
    private lateinit var userId: String
    private lateinit var marca: String
    private lateinit var modelo: String
    private lateinit var placa: String
    private lateinit var selectedLine: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_register_driver)

        userId = intent.getStringExtra("userId") ?: ""
        if (userId.isEmpty()) {
            Log.e(TAG, "User ID is missing")
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Solicitar permisos si no están concedidos
        checkPermissions()

        initComponents()
        initListeners()
    }

    private fun initComponents() {
        cvCarnetDeIdentidad = findViewById(R.id.cvCarnetDeIdentidad)
        cvDriverLicense = findViewById(R.id.cvDriverLicense)
        cvDataVehicle = findViewById(R.id.cvDataVehicle)
        btnFinishRegister = findViewById(R.id.btnFinishRegister)
    }

    private fun initListeners() {
        cvCarnetDeIdentidad.setOnClickListener {
            showCameraLabelDialog("Saca foto de la parte frontal de tu CI", REQUEST_IMAGE_CAPTURE_1)
        }
        cvDriverLicense.setOnClickListener {
            showCameraLabelDialog("Saca foto de la parte frontal de tu licencia de conducir", REQUEST_IMAGE_CAPTURE_3)
        }
        cvDataVehicle.setOnClickListener {
            val intent = Intent(this, VehicleFormActivity::class.java)
            intent.putExtra("userId", userId)
            startActivityForResult(intent, REQUEST_VEHICLE_FORM)
        }
        btnFinishRegister.setOnClickListener {
            sendDriverData()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showCameraLabelDialog(message: String, requestCode: Int) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE_1 -> {
                    imageUri1 = data?.data
                    showCameraLabelDialog("Saca foto de la parte posterior de tu CI", REQUEST_IMAGE_CAPTURE_2)
                }
                REQUEST_IMAGE_CAPTURE_2 -> {
                    imageUri2 = data?.data
                    // Aquí puedes manejar la imagen capturada del carnet de identidad
                }
                REQUEST_IMAGE_CAPTURE_3 -> {
                    imageUri3 = data?.data
                    showCameraLabelDialog("Saca foto de la parte posterior de tu licencia de conducir", REQUEST_IMAGE_CAPTURE_4)
                }
                REQUEST_IMAGE_CAPTURE_4 -> {
                    imageUri4 = data?.data
                    // Aquí puedes manejar la imagen capturada de la licencia de conducir
                }
                REQUEST_VEHICLE_FORM -> {
                    marca = data?.getStringExtra("marca") ?: ""
                    modelo = data?.getStringExtra("modelo") ?: ""
                    placa = data?.getStringExtra("placa") ?: ""
                    imageUriVehicle = Uri.parse(data?.getStringExtra("imageUriVehicle"))
                    imageUriRUAT = Uri.parse(data?.getStringExtra("imageUriRUAT"))
                    selectedLine = data?.getStringExtra("selectedLine") ?: ""
                }
            }
        }
    }

    private fun sendDriverData() {
        val url = "https://ruta-xpress-backend-express-js.vercel.app/driver-routes/createDriver"

        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("idUser", userId)
            .addFormDataPart("Marcca", marca)
            .addFormDataPart("Model", modelo)
            .addFormDataPart("Placa", placa)
            .addFormDataPart("routes", selectedLine)

        addImagePart(multipartBody, "photoCi", imageUri1)
        addImagePart(multipartBody, "reversePhotoCi", imageUri2)
        addImagePart(multipartBody, "photoLicence", imageUri3)
        addImagePart(multipartBody, "reversePhotoLicence", imageUri4)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(multipartBody.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error al enviar datos: ${e.message}")
                runOnUiThread { Toast.makeText(this@RegisterDriverActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterDriverActivity, "Datos enviados correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Error del servidor: ${response.message()}")
                        Toast.makeText(this@RegisterDriverActivity, "Error del servidor: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    private fun addImagePart(multipartBody: MultipartBody.Builder, name: String, uri: Uri?) {
        uri?.let {
            val file = getFileFromUri(this, it)
            if (file != null) {
                multipartBody.addFormDataPart(name, file.name, RequestBody.create(MediaType.parse("image/jpeg"), file))
            } else {
                Log.e(TAG, "No se pudo convertir Uri a File: $uri")
            }
        }
    }
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo Uri a File: ${e.message}")
            null
        }
    }
    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}