package com.example.nvtrilife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class SolicitudDetalleActivity : AppCompatActivity() {

    private lateinit var idNutriologa: String
    private lateinit var btnAprobar: Button
    private lateinit var btnRechazar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitud_detalle)
        aplicarInsets()
        configurarBarra()

        idNutriologa = intent.getStringExtra("id") ?: run { finish(); return }

        findViewById<TextView>(R.id.tvNombre).text = intent.getStringExtra("nombre")
        findViewById<TextView>(R.id.tvCedula).text =
            "Cédula profesional: ${intent.getStringExtra("cedula")}"
        findViewById<TextView>(R.id.tvTelefono).text =
            "Teléfono: ${intent.getStringExtra("telefono") ?: "—"}"

        btnAprobar = findViewById(R.id.btnAprobar)
        btnRechazar = findViewById(R.id.btnRechazar)

        findViewById<Button>(R.id.btnVerDocumento).setOnClickListener { verDocumento() }
        btnAprobar.setOnClickListener { decidir("aprobada") }
        btnRechazar.setOnClickListener { decidir("rechazada") }
    }

    private fun verDocumento() {
        lifecycleScope.launch {
            try {
                val url = supabase.storage.from("documentos")
                    .createSignedUrl("$idNutriologa/cedula.pdf", 5.minutes)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                mensaje("No se pudo abrir el documento: ${e.message}")
            }
        }
    }

    private fun decidir(estado: String) {
        btnAprobar.isEnabled = false
        btnRechazar.isEnabled = false
        lifecycleScope.launch {
            try {
                AuthRepository.cambiarEstado(idNutriologa, estado)
                mensaje(if (estado == "aprobada") "Nutrióloga aprobada" else "Solicitud rechazada")
                finish()
            } catch (e: Exception) {
                mensaje("Error: ${e.message}")
                btnAprobar.isEnabled = true
                btnRechazar.isEnabled = true
            }
        }
    }
}