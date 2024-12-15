package com.softcraft.rutaxpressapp.driverView

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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.softcraft.rutaxpressapp.R
import java.io.ByteArrayOutputStream

class RegisterDriverActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE_1 = 1
        private const val REQUEST_IMAGE_CAPTURE_2 = 2
        private const val TAG = "RegisterDriverActivity"
    }
    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null
    private lateinit var cvCarnetDeIdentidad: CardView
    private lateinit var cvDriverLicense: CardView
    private lateinit var cvDataVehicle: CardView
    private lateinit var cvSoat: CardView
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

        //initCloudinary()
        initComponents()
        initListeners()
    }

//    private fun initCloudinary() {
//        try {
//            MediaManager.get()
//        } catch (e: IllegalStateException) {
//            val config = mapOf(
//                "cloud_name" to "djcfm4nd2",
//                "api_key" to "897657815927312",
//                "api_secret" to "6Af5mOu8kiKfn9MT-P3Ag6vXF1s"
//            )
//            MediaManager.init(this, config)
//        }
//    }

    private fun initComponents() {
        cvCarnetDeIdentidad = findViewById(R.id.cvCarnetDeIdentidad)
        cvDriverLicense = findViewById(R.id.cvDriverLicense)
        cvDataVehicle = findViewById(R.id.cvDataVehicle)
        cvSoat = findViewById(R.id.cvSoat)
        btnFinishRegister = findViewById(R.id.btnFinishRegister)
    }

    private fun initListeners() {
        cvCarnetDeIdentidad.setOnClickListener {
            showCameraLabelDialog("Saca foto de la parte frontal de tu CI", REQUEST_IMAGE_CAPTURE_1)
        }
        cvDriverLicense.setOnClickListener {

        }
        cvDataVehicle.setOnClickListener {

        }
        cvSoat.setOnClickListener {

        }
        btnFinishRegister.setOnClickListener {
            val intent = Intent(this, InitialMapDriverActivity::class.java)
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
                    uploadImagesToCloudinary()
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

    private fun uploadImagesToCloudinary() {
        if (imageUri1 != null && imageUri2 != null) {
            val compressedImage1 = compressImage(imageUri1!!)
            val compressedImage2 = compressImage(imageUri2!!)

            MediaManager.get().upload(compressedImage1)
                .option("public_id", "driverCI/${userId}_front")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val frontImageUrl = resultData["secure_url"] as String
                        Log.d(TAG, "Front image uploaded: $frontImageUrl")
                        MediaManager.get().upload(compressedImage2)
                            .option("public_id", "driverCI/${userId}_back")
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String) {}

                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                    val backImageUrl = resultData["secure_url"] as String
                                    Log.d(TAG, "Back image uploaded: $backImageUrl")
                                    saveDriverCIToFirestore(frontImageUrl, backImageUrl)
                                }

                                override fun onError(requestId: String, error: ErrorInfo) {
                                    Log.e(TAG, "Error uploading back image: ${error.description}")
                                    Toast.makeText(this@RegisterDriverActivity, "Error al subir la imagen: ${error.description}", Toast.LENGTH_SHORT).show()
                                }

                                override fun onReschedule(requestId: String, error: ErrorInfo) {}
                            })
                            .dispatch()
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Error uploading front image: ${error.description}")
                        Toast.makeText(this@RegisterDriverActivity, "Error al subir la imagen: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        }
    }

    private fun saveDriverCIToFirestore(frontImageUrl: String, backImageUrl: String) {
        val driverCI = hashMapOf(
            "userId" to userId,
            "frontImageUrl" to frontImageUrl,
            "backImageUrl" to backImageUrl
        )

        val db = FirebaseFirestore.getInstance()
        val driverCIRef = db.collection("DriverCI").document(userId)

        driverCIRef.set(driverCI).addOnSuccessListener {
            Log.d(TAG, "Images saved to Firestore")
            Toast.makeText(this, "Imágenes guardadas exitosamente", Toast.LENGTH_SHORT).show()
            // Aquí puedes navegar a otra actividad si es necesario
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error saving images to Firestore: ${e.message}")
            Toast.makeText(this, "Error al guardar las imágenes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}