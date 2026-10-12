package com.example.nvtrilife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroNutriologaActivity : AppCompatActivity() {

    private var pdfBytes: ByteArray? = null

    private val elegirPdf = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            lifecycleScope.launch {
                pdfBytes = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                findViewById<TextView>(R.id.tvArchivo).text = "✓ ${nombreArchivo(uri)}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_nutriologa)
        aplicarInsets()
        configurarBarra()

        fun txt(id: Int) = findViewById<TextInputEditText>(id).text.toString().trim()

        val btn = findViewById<Button>(R.id.btnRegistrar)
        val progress = findViewById<ProgressBar>(R.id.progress)

        findViewById<Button>(R.id.btnArchivo).setOnClickListener {
            elegirPdf.launch("application/pdf")
        }
        findViewById<View>(R.id.tvYaTienes).setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        btn.setOnClickListener {
            val nombreCompleto = txt(R.id.etNombre)
            val correo = txt(R.id.etCorreo)
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString()
            val cedula = txt(R.id.etCedula)
            val archivo = pdfBytes

            if (nombreCompleto.split(Regex("\\s+")).size < 2) {
                mensaje("Escribe tu nombre completo (nombre y apellidos)"); return@setOnClickListener
            }
            if (correo.isEmpty()) {
                mensaje("Escribe tu correo"); return@setOnClickListener
            }
            if (password.length < 6) {
                mensaje("La contraseña debe tener al menos 6 caracteres"); return@setOnClickListener
            }
            if (cedula.isEmpty()) {
                mensaje("Escribe tu cédula profesional"); return@setOnClickListener
            }
            if (archivo == null) {
                mensaje("Adjunta el PDF que compruebe tu cédula"); return@setOnClickListener
            }

            val (nombre, apellidos) = separarNombre(nombreCompleto)
            val telefono = txt(R.id.etTelefono).ifEmpty { null }

            btn.isEnabled = false
            progress.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    AuthRepository.registrarNutriologa(
                        correo, password, nombre, apellidos,
                        null, null, telefono, cedula, archivo
                    )
                    AuthRepository.cerrarSesion()
                    startActivity(
                        Intent(this@RegistroNutriologaActivity, EnRevisionActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                } catch (e: Exception) {
                    val m = e.message.orEmpty()
                    mensaje(
                        if (m.contains("already", ignoreCase = true)) "El correo ya se encuentra registrado"
                        else "Error al registrar: $m"
                    )
                    btn.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }

    private fun nombreArchivo(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) return c.getString(i)
        }
        return "archivo.pdf"
    }
}