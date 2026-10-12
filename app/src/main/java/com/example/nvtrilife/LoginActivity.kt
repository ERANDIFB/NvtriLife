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

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        aplicarInsets()

        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnIniciarSesion)
        val progress = findViewById<ProgressBar>(R.id.progress)

        findViewById<View>(R.id.tvCrearCuenta).setOnClickListener {
            startActivity(Intent(this, UneteActivity::class.java))
        }

        btn.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()
            if (correo.isEmpty() || password.isEmpty()) {
                mensaje("Escribe tu correo y contraseña")
                return@setOnClickListener
            }
            btn.isEnabled = false
            progress.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    when (val destino = AuthRepository.iniciarSesion(correo, password)) {
                        Destino.Admin -> irA(AdminHomeActivity::class.java)
                        Destino.InicioNutriologa -> irA(NutriologaHomeActivity::class.java)
                        Destino.InicioPaciente -> irA(PacienteHomeActivity::class.java)
                        is Destino.EnRevision -> {
                            AuthRepository.cerrarSesion()
                            if (destino.estado == "rechazada") {
                                mensaje("Tu solicitud fue rechazada. Contacta a la administradora.")
                            } else {
                                startActivity(Intent(this@LoginActivity, EnRevisionActivity::class.java))
                            }
                        }
                    }
                } catch (e: Exception) {
                    val m = e.message.orEmpty()
                    mensaje(
                        if (m.contains("invalid", ignoreCase = true)) "Usuario o contraseña incorrectos"
                        else "No se pudo iniciar sesión: $m"
                    )
                } finally {
                    btn.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }

    private fun irA(destino: Class<*>) {
        startActivity(
            Intent(this, destino)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}
