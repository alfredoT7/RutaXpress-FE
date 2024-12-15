package com.softcraft.rutaxpressapp.viewsDriver

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.softcraft.rutaxpressapp.R

class QrPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_payment)

        // Datos para el código QR
        val qrData = "ConductorID:12345;Monto:2;Moneda:BNB"

        try {
            // Generar el código QR
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400)

            // Mostrar el código QR en el ImageView
            val imageViewQrCode: ImageView = findViewById(R.id.imageViewQrCode)
            imageViewQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
