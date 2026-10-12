package com.example.nvtrilife

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val id: String,
    val rol: String,
    val nombre: String,
    val apellidos: String,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    val sexo: String? = null,
    val telefono: String? = null
)

@Serializable
data class Nutriologa(
    val id: String,
    @SerialName("cedula_prof") val cedulaProf: String,
    @SerialName("estado_cuenta") val estadoCuenta: String,
    @SerialName("codigo_vinculacion") val codigoVinculacion: String? = null
)

@Serializable
data class NutriologaNueva(
    val id: String,
    @SerialName("cedula_prof") val cedulaProf: String,
    @SerialName("estado_cuenta") val estadoCuenta: String
)

@Serializable
data class Paciente(
    val id: String,
    @SerialName("id_nutriologa") val idNutriologa: String? = null
)

@Serializable
data class DocumentoProfesional(
    @SerialName("id_nutriologa") val idNutriologa: String,
    @SerialName("archivo_url") val archivoUrl: String
)

@Serializable
data class PerfilBasico(
    val nombre: String,
    val apellidos: String,
    val telefono: String? = null
)

@Serializable
data class NutriologaPendiente(
    val id: String,
    @SerialName("cedula_prof") val cedulaProf: String,
    val perfiles: PerfilBasico
)

@Serializable
data class PacienteConNombre(
    val id: String,
    val perfiles: PerfilBasico
)

@Serializable
data class ParamCodigo(val codigo: String)