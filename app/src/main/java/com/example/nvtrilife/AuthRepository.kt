package com.example.nvtrilife

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage

sealed class Destino {
    object Admin : Destino()
    object InicioNutriologa : Destino()
    data class EnRevision(val estado: String) : Destino()
    object InicioPaciente : Destino()
}

object AuthRepository {

    suspend fun iniciarSesion(correo: String, contrasena: String): Destino {
        supabase.auth.signInWith(Email) {
            email = correo
            password = contrasena
        }
        val id = supabase.auth.currentUserOrNull()!!.id
        val perfil = supabase.from("perfiles")
            .select { filter { eq("id", id) } }
            .decodeSingle<Perfil>()

        return when (perfil.rol) {
            "administradora" -> Destino.Admin
            "nutriologa" -> {
                val n = supabase.from("nutriologas")
                    .select { filter { eq("id", id) } }
                    .decodeSingle<Nutriologa>()
                if (n.estadoCuenta == "aprobada") Destino.InicioNutriologa
                else Destino.EnRevision(n.estadoCuenta)
            }
            else -> Destino.InicioPaciente
        }
    }

    suspend fun registrarPaciente(
        correo: String, contrasena: String,
        nombre: String, apellidos: String,
        fechaNacimiento: String?, sexo: String?, telefono: String?
    ) {
        supabase.auth.signUpWith(Email) {
            email = correo
            password = contrasena
        }
        val id = supabase.auth.currentUserOrNull()!!.id
        supabase.from("perfiles").insert(
            Perfil(id, "paciente", nombre, apellidos, fechaNacimiento, sexo, telefono)
        )
        supabase.from("pacientes").insert(Paciente(id))
    }

    suspend fun registrarNutriologa(
        correo: String, contrasena: String,
        nombre: String, apellidos: String,
        fechaNacimiento: String?, sexo: String?, telefono: String?,
        cedula: String, archivoCedula: ByteArray
    ) {
        supabase.auth.signUpWith(Email) {
            email = correo
            password = contrasena
        }
        val id = supabase.auth.currentUserOrNull()!!.id
        supabase.from("perfiles").insert(
            Perfil(id, "nutriologa", nombre, apellidos, fechaNacimiento, sexo, telefono)
        )
        supabase.from("nutriologas").insert(NutriologaNueva(id, cedula, "pendiente"))

        val ruta = "$id/cedula.pdf"
        supabase.storage.from("documentos").upload(ruta, archivoCedula) { upsert = true }
        supabase.from("documentos_profesionales").insert(DocumentoProfesional(id, ruta))
    }

    suspend fun listarPendientes(): List<NutriologaPendiente> =
        supabase.from("nutriologas")
            .select(Columns.raw("id, cedula_prof, perfiles(nombre, apellidos)")) {
                filter { eq("estado_cuenta", "pendiente") }
            }
            .decodeList<NutriologaPendiente>()

    suspend fun cambiarEstado(idNutriologa: String, estado: String) {
        supabase.from("nutriologas").update({ set("estado_cuenta", estado) }) {
            filter { eq("id", idNutriologa) }
        }
    }

    suspend fun miCodigo(): String {
        val id = supabase.auth.currentUserOrNull()!!.id
        return supabase.from("nutriologas")
            .select { filter { eq("id", id) } }
            .decodeSingle<Nutriologa>()
            .codigoVinculacion ?: ""
    }

    suspend fun misPacientes(): List<PacienteConNombre> =
        supabase.from("pacientes")
            .select(Columns.raw("id, perfiles(nombre, apellidos, telefono)"))
            .decodeList<PacienteConNombre>()

    suspend fun yaEstoyVinculado(): Boolean {
        val id = supabase.auth.currentUserOrNull()!!.id
        val p = supabase.from("pacientes")
            .select { filter { eq("id", id) } }
            .decodeSingle<Paciente>()
        return p.idNutriologa != null
    }

    suspend fun vincularConCodigo(codigo: String): String =
        supabase.postgrest.rpc("vincular_con_codigo", ParamCodigo(codigo))
            .decodeAs<String>()

    suspend fun cerrarSesion() = supabase.auth.signOut()
}