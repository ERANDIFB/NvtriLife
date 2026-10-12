package com.example.nvtrilife

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var contenedor: LinearLayout
    private lateinit var tvVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)
        aplicarInsets()
        contenedor = findViewById(R.id.contenedor)
        tvVacio = findViewById(R.id.tvVacio)
        findViewById<View>(R.id.btnSalir).setOnClickListener { cerrarSesionYVolver() }
    }

    // Se recarga al volver del detalle, para que desaparezcan las ya revisadas.
    override fun onResume() {
        super.onResume()
        cargar()
    }

    private fun cargar() {
        lifecycleScope.launch {
            try {
                val lista = AuthRepository.listarPendientes()
                contenedor.removeAllViews()
                tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE

                for (n in lista) {
                    val item = LayoutInflater.from(this@AdminHomeActivity)
                        .inflate(R.layout.item_pendiente, contenedor, false)
                    val nombre = "${n.perfiles.nombre} ${n.perfiles.apellidos}"
                    item.findViewById<TextView>(R.id.tvNombre).text = nombre
                    item.findViewById<TextView>(R.id.tvCedula).text = "Cédula: ${n.cedulaProf}"
                    item.setOnClickListener {
                        startActivity(
                            Intent(this@AdminHomeActivity, SolicitudDetalleActivity::class.java)
                                .putExtra("id", n.id)
                                .putExtra("nombre", nombre)
                                .putExtra("cedula", n.cedulaProf)
                                .putExtra("telefono", n.perfiles.telefono)
                        )
                    }
                    contenedor.addView(item)
                }
            } catch (e: Exception) {
                mensaje("Error al cargar: ${e.message}")
            }
        }
    }
}