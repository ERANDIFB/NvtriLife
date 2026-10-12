package com.example.nvtrilife

import android.app.DatePickerDialog
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/** Evita que la barra de estado tape el contenido. Requiere un View con id "raiz". */
fun AppCompatActivity.aplicarInsets() {
    val raiz = findViewById<View>(R.id.raiz)
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
        val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(b.left, b.top, b.right, b.bottom)
        insets
    }
}

/** La barra verde de arriba (id "barra") funciona como botón de regresar. */
fun AppCompatActivity.configurarBarra() {
    findViewById<View>(R.id.barra)?.setOnClickListener { finish() }
}

fun AppCompatActivity.mensaje(texto: String) =
    Toast.makeText(this, texto, Toast.LENGTH_LONG).show()

fun AppCompatActivity.configurarFecha(et: TextInputEditText) {
    et.setOnClickListener {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            et.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d))
        }, c.get(Calendar.YEAR) - 25, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }
}

fun AppCompatActivity.cerrarSesionYVolver() {
    lifecycleScope.launch {
        try { AuthRepository.cerrarSesion() } catch (_: Exception) { }
        startActivity(
            Intent(this@cerrarSesionYVolver, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}

/** "Ana María Gómez García" -> ("Ana María", "Gómez García"). Requiere al menos 2 palabras. */
fun separarNombre(completo: String): Pair<String, String> {
    val p = completo.trim().split(Regex("\\s+"))
    return when {
        p.size == 1 -> p[0] to ""
        p.size == 2 -> p[0] to p[1]
        p.size == 3 -> p[0] to "${p[1]} ${p[2]}"
        else -> p.dropLast(2).joinToString(" ") to p.takeLast(2).joinToString(" ")
    }
}