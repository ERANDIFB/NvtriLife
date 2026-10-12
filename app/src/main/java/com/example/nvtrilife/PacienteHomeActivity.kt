package com.example.nvtrilife

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PacienteHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_simple)
        aplicarInsets()
        findViewById<TextView>(R.id.tvHola).text = "¡Hola, paciente!"
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener { cerrarSesionYVolver() }
    }
}