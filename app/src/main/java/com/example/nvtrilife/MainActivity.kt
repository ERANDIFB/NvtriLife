package com.example.nvtrilife

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aplicarInsets()

        findViewById<Button>(R.id.btnIniciarSesion).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.btnCrearCuenta).setOnClickListener {
            startActivity(Intent(this, UneteActivity::class.java))
        }
    }
}