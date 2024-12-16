package com.softcraft.rutaxpressapp.viewsDriver

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.softcraft.rutaxpressapp.LoginActivity
import com.softcraft.rutaxpressapp.R
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class RegisterDriverActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE_1 = 1
        private const val REQUEST_IMAGE_CAPTURE_2 = 2
        private const val REQUEST_IMAGE_CAPTURE_3 = 3
        private const val REQUEST_IMAGE_CAPTURE_4 = 4
        private const val REQUEST_VEHICLE_FORM = 5
        private const val TAG = "RegisterDriverActivity"
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

        Log.d(TAG, "User ID: $userId")

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
            val intent = Intent(this@RegisterDriverActivity, VehicleFormActivity::class.java)
            intent.putExtra("userId", userId)
            startActivityForResult(intent, REQUEST_VEHICLE_FORM)
        }
        btnFinishRegister.setOnClickListener {
            sendDriverData()
            intent = Intent(this@RegisterDriverActivity, LoginActivity::class.java)
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

        imageUri1?.path?.let {
            Log.d(TAG, "Adding photoCi: $it")
            val file = File(it)
            if (file.exists()) {
                multipartBody.addFormDataPart("photoCi", "photoCi.jpg", RequestBody.create(MediaType.parse("image/jpeg"), file))
            } else {
                Log.e(TAG, "File for imageUri1 does not exist: $it")
            }
        } ?: Log.e(TAG, "imageUri1 is null")

        imageUri2?.path?.let {
            Log.d(TAG, "Adding reversePhotoCi: $it")
            val file = File(it)
            if (file.exists()) {
                multipartBody.addFormDataPart("reversePhotoCi", "reversePhotoCi.jpg", RequestBody.create(MediaType.parse("image/jpeg"), file))
            } else {
                Log.e(TAG, "File for imageUri2 does not exist: $it")
            }
        } ?: Log.e(TAG, "imageUri2 is null")

        imageUri3?.path?.let {
            Log.d(TAG, "Adding photoLicence: $it")
            val file = File(it)
            if (file.exists()) {
                multipartBody.addFormDataPart("photoLicence", "photoLicence.jpg", RequestBody.create(MediaType.parse("image/jpeg"), file))
            } else {
                Log.e(TAG, "File for imageUri3 does not exist: $it")
            }
        } ?: Log.e(TAG, "imageUri3 is null")

        imageUri4?.path?.let {
            Log.d(TAG, "Adding reversePhotoLicence: $it")
            val file = File(it)
            if (file.exists()) {
                multipartBody.addFormDataPart("reversePhotoLicence", "reversePhotoLicence.jpg", RequestBody.create(MediaType.parse("image/jpeg"), file))
            } else {
                Log.e(TAG, "File for imageUri4 does not exist: $it")
            }
        } ?: Log.e(TAG, "imageUri4 is null")

        imageUriVehicle?.path?.let {
            Log.d(TAG, "Adding photoCar: $it")
            val file = File(it)
            if (file.exists()) {
                multipartBody.addFormDataPart("photoCar", "photoCar.jpg", RequestBody.create(MediaType.parse("image/jpeg"), file))
            } else {
                Log.e(TAG, "File for imageUriVehicle does not exist: $it")
            }
        } ?: Log.e(TAG, "imageUriVehicle is null")

        imageUriRUAT?.path?.let {
            Log.d(TAG, "Adding RUAT: $it")
            val file = File(it)
            if (file.exists()) {
                multipartBody.addFormDataPart("RUAT", "RUAT.jpg", RequestBody.create(MediaType.parse("image/jpeg"), file))
            } else {
                Log.e(TAG, "File for imageUriRUAT does not exist: $it")
            }
        } ?: Log.e(TAG, "imageUriRUAT is null")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(multipartBody.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error al enviar datos: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@RegisterDriverActivity, "Error al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Datos enviados exitosamente")
                        Toast.makeText(this@RegisterDriverActivity, "Datos enviados exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Error al enviar datos: ${response.body()}")
                        Toast.makeText(this@RegisterDriverActivity, "Error al enviar datos: ${response.body()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}