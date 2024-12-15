package com.softcraft.rutaxpressapp.viewsDriver

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.softcraft.rutaxpressapp.LoginActivity
import com.softcraft.rutaxpressapp.R
import java.io.ByteArrayOutputStream

class VehicleFormActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE_VEHICLE = 1
        private const val REQUEST_IMAGE_CAPTURE_RUAT = 2
        private const val TAG = "VehicleFormActivity"
    }

    private var imageUriVehicle: Uri? = null
    private var imageUriRUAT: Uri? = null
    private lateinit var etMarca: EditText
    private lateinit var etModelo: EditText
    private lateinit var btnFotoVehiculo: Button
    private lateinit var btnFotoRUAT: Button
    private lateinit var btnFinishRegister: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_vehicle_form)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initComponents()
        initListeners()
    }

    private fun initComponents() {
        etMarca = findViewById(R.id.etMarca)
        etModelo = findViewById(R.id.etModelo)
        btnFotoVehiculo = findViewById(R.id.btnFotoVehiculo)
        btnFotoRUAT = findViewById(R.id.btnFotoRUAT)
        btnFinishRegister = findViewById(R.id.btnFinishRegister)
    }

    private fun initListeners() {
        btnFotoVehiculo.setOnClickListener {
            showCameraLabelDialog("Saca foto del vehículo", REQUEST_IMAGE_CAPTURE_VEHICLE)
        }
        btnFotoRUAT.setOnClickListener {
            showCameraLabelDialog("Saca foto del RUAT", REQUEST_IMAGE_CAPTURE_RUAT)
        }
        btnFinishRegister.setOnClickListener {
            saveVehicleData()
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
                REQUEST_IMAGE_CAPTURE_VEHICLE -> {
                    imageUriVehicle = data?.data
                }
                REQUEST_IMAGE_CAPTURE_RUAT -> {
                    imageUriRUAT = data?.data
                }
            }
        }
    }

    private fun saveVehicleData() {
        val marca = etMarca.text.toString()
        val modelo = etModelo.text.toString()
        val userId = auth.currentUser?.uid

        if (marca.isEmpty() || modelo.isEmpty() || userId == null) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val vehicleData = hashMapOf(
            "userId" to userId,
            "marca" to marca,
            "modelo" to modelo,
            "imageUriVehicle" to imageUriVehicle.toString(),
            "imageUriRUAT" to imageUriRUAT.toString()
        )

        db.collection("vehicles").add(vehicleData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registro de vehículo exitoso", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al registrar el vehículo", e)
                Toast.makeText(this, "Error al registrar el vehículo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return outputStream.toByteArray()
    }
}