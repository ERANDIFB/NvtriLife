package com.example.nvtrilife

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegistroPacienteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_paciente)
        aplicarInsets()
        configurarBarra()

        fun txt(id: Int) = findViewById<TextInputEditText>(id).text.toString().trim()
        configurarFecha(findViewById(R.id.etFecha))

        val btn = findViewById<Button>(R.id.btnRegistrar)
        val progress = findViewById<ProgressBar>(R.id.progress)

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

            if (nombreCompleto.split(Regex("\\s+")).size < 2) {
                mensaje("Escribe tu nombre completo (nombre y apellidos)"); return@setOnClickListener
            }
            if (correo.isEmpty()) {
                mensaje("Escribe tu correo"); return@setOnClickListener
            }
            if (password.length < 6) {
                mensaje("La contraseña debe tener al menos 6 caracteres"); return@setOnClickListener
            }

            val (nombre, apellidos) = separarNombre(nombreCompleto)
            val fecha = txt(R.id.etFecha).ifEmpty { null }
            val telefono = txt(R.id.etTelefono).ifEmpty { null }

            btn.isEnabled = false
            progress.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    AuthRepository.registrarPaciente(
                        correo, password, nombre, apellidos, fecha, null, telefono
                    )
                    startActivity(
                        Intent(this@RegistroPacienteActivity, PacienteHomeActivity::class.java)
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
}