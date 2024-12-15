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
import com.softcraft.rutaxpressapp.R
import java.io.ByteArrayOutputStream

class RegisterDriverActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE_1 = 1
        private const val REQUEST_IMAGE_CAPTURE_2 = 2
        private const val REQUEST_IMAGE_CAPTURE_3 = 3
        private const val REQUEST_IMAGE_CAPTURE_4 = 4
        private const val TAG = "RegisterDriverActivity"
    }
    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null
    private var imageUri3: Uri? = null
    private var imageUri4: Uri? = null
    private lateinit var cvCarnetDeIdentidad: CardView
    private lateinit var cvDriverLicense: CardView
    private lateinit var cvDataVehicle: CardView
    private lateinit var cvTransportLine: CardView
    private lateinit var btnFinishRegister: Button
    private lateinit var userId: String

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
        cvTransportLine = findViewById(R.id.cvTransportLine)
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

        }
        cvTransportLine.setOnClickListener {

        }
        btnFinishRegister.setOnClickListener {
            val intent = Intent(this@RegisterDriverActivity, InitialDriverActivity::class.java)
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
            }
        }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return outputStream.toByteArray()
    }
}