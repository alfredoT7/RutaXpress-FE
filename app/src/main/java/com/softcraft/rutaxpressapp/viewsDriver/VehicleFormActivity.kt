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
import com.softcraft.rutaxpressapp.LineasFilterActivity
import com.softcraft.rutaxpressapp.LoginActivity
import com.softcraft.rutaxpressapp.R
import java.io.ByteArrayOutputStream

class VehicleFormActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE_VEHICLE = 1
        private const val REQUEST_IMAGE_CAPTURE_RUAT = 2
        private const val REQUEST_CODE_SELECT_LINE = 3
        private const val TAG = "VehicleFormActivity"
    }

    private var imageUriVehicle: Uri? = null
    private var imageUriRUAT: Uri? = null
    private lateinit var etMarca: EditText
    private lateinit var etModelo: EditText
    private lateinit var etPlaca: EditText
    private lateinit var btnFotoVehiculo: Button
    private lateinit var btnFotoRUAT: Button
    private lateinit var btnFinishRegister: Button
    private lateinit var btnChooseLine: Button
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
        etPlaca = findViewById(R.id.etPlaca)
        btnFotoVehiculo = findViewById(R.id.btnFotoVehiculo)
        btnFotoRUAT = findViewById(R.id.btnFotoRUAT)
        btnFinishRegister = findViewById(R.id.btnFinishRegister)
        btnChooseLine = findViewById(R.id.btnChooseLine)
    }

    private fun initListeners() {
        btnFotoVehiculo.setOnClickListener {
            showCameraLabelDialog("Saca foto del vehículo", REQUEST_IMAGE_CAPTURE_VEHICLE)
        }
        btnFotoRUAT.setOnClickListener {
            showCameraLabelDialog("Saca foto del RUAT", REQUEST_IMAGE_CAPTURE_RUAT)
        }
        btnChooseLine.setOnClickListener {
            val intent = Intent(this, LineasFilterActivity::class.java)
            intent.putExtra("isDriver", true)
            startActivityForResult(intent, REQUEST_CODE_SELECT_LINE)
        }
        btnFinishRegister.setOnClickListener {
            val data = Intent().apply {
                putExtra("marca", etMarca.text.toString())
                putExtra("modelo", etModelo.text.toString())
                putExtra("placa", etPlaca.text.toString())
                putExtra("imageUriVehicle", imageUriVehicle.toString())
                putExtra("imageUriRUAT", imageUriRUAT.toString())
            }
            setResult(RESULT_OK, data)
            finish()
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
                REQUEST_CODE_SELECT_LINE -> {
                    val selectedLine = data?.getStringExtra("selectedLine")
                    Toast.makeText(this, "Línea seleccionada: $selectedLine", Toast.LENGTH_SHORT).show()
                }
                REQUEST_IMAGE_CAPTURE_VEHICLE -> {
                    imageUriVehicle = data?.data
                }
                REQUEST_IMAGE_CAPTURE_RUAT -> {
                    imageUriRUAT = data?.data
                }
            }
        }
    }
}