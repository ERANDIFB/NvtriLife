package com.example.nvtrilife

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class UneteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unete)
        aplicarInsets()

        findViewById<Button>(R.id.btnNutriologo).setOnClickListener {
            startActivity(Intent(this, RegistroNutriologaActivity::class.java))
        }
        findViewById<Button>(R.id.btnPaciente).setOnClickListener {
            startActivity(Intent(this, RegistroPacienteActivity::class.java))
        }
    }
}