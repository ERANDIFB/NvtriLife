package com.example.nvtrilife

import io.github.jan.supabase.auth.currentUserOrNull
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate

// =====================================================================
// REPOSITORIO DE DATOS
// NvtriLife - Supabase
// =====================================================================

object DatosRepository {

    // ---------------------------------------------------------------
    // USUARIO AUTENTICADO
    // ---------------------------------------------------------------

    private fun miId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: error("No hay un usuario autenticado.")

    // ===============================================================
    // 1. PACIENTE: SEGUIMIENTO DIARIO
    // ===============================================================

    // Guarda o actualiza el seguimiento de un día.
    suspend fun guardarSeguimiento(s: SeguimientoDiario) {
        val fecha = s.fecha.ifBlank {
            LocalDate.now().toString()
        }

        supabase.from("seguimiento_diario").upsert(
            s.copy(
                idPaciente = miId(),
                fecha = fecha
            )
        ) {
            onConflict = "id_paciente,fecha"
        }
    }

    // Consulta el seguimiento del día actual.
    suspend fun seguimientoDeHoy(): SeguimientoDiario? =
        supabase.from("seguimiento_diario").select {
            filter {
                eq("id_paciente", miId())
                eq("fecha", LocalDate.now().toString())
            }
        }.decodeList<SeguimientoDiario>().firstOrNull()

    // Consulta el historial de seguimientos del paciente.
    suspend fun miHistorial(): List<SeguimientoDiario> =
        supabase.from("seguimiento_diario").select {
            filter {
                eq("id_paciente", miId())
            }
            order("fecha", Order.DESCENDING)
        }.decodeList()

    // Consulta los seguimientos de un paciente desde la cuenta
    // de la nutrióloga.
    suspend fun seguimientoDePaciente(
        idPaciente: String
    ): List<SeguimientoDiario> =
        supabase.from("seguimiento_diario").select {
            filter {
                eq("id_paciente", idPaciente)
            }
            order("fecha", Order.DESCENDING)
        }.decodeList()

    // ===============================================================
    // 2. EXPEDIENTES
    // ===============================================================

    // El paciente consulta su expediente.
    suspend fun miExpediente(): Expediente? =
        supabase.from("expedientes").select {
            filter {
                eq("id_paciente", miId())
            }
        }.decodeList<Expediente>().firstOrNull()

    // La nutrióloga consulta el expediente de un paciente.
    suspend fun expedienteDe(
        idPaciente: String
    ): Expediente? =
        supabase.from("expedientes").select {
            filter {
                eq("id_paciente", idPaciente)
            }
        }.decodeList<Expediente>().firstOrNull()

    // La nutrióloga crea o actualiza el expediente.
    suspend fun guardarExpediente(e: ExpedienteNuevo) {
        supabase.from("expedientes").upsert(e) {
            onConflict = "id_paciente"
        }
    }

    // La nutrióloga elimina el expediente de un paciente.
    suspend fun eliminarExpediente(idPaciente: String) {
        supabase.from("expedientes").delete {
            filter {
                eq("id_paciente", idPaciente)
            }
        }
    }

    // ===============================================================
    // 3. CITAS
    // ===============================================================

    // El paciente consulta sus citas.
    suspend fun misCitas(): List<Cita> =
        supabase.from("citas").select {
            filter {
                eq("id_paciente", miId())
            }
            order("fecha", Order.ASCENDING)
            order("hora", Order.ASCENDING)
        }.decodeList()

    // La nutrióloga consulta su agenda.
    // Si se indica una fecha, consulta solamente ese día.
    suspend fun citasDeNutriologa(
        fecha: String? = null
    ): List<Cita> =
        supabase.from("citas").select {
            filter {
                eq("id_nutriologa", miId())

                if (fecha != null) {
                    eq("fecha", fecha)
                }
            }
            order("fecha", Order.ASCENDING)
            order("hora", Order.ASCENDING)
        }.decodeList()

    // La nutrióloga agenda una cita.
    suspend fun crearCita(
        idPaciente: String,
        fecha: String,
        hora: String,
        motivo: String?
    ) {
        supabase.from("citas").insert(
            CitaNueva(
                miId(),
                idPaciente,
                fecha,
                hora,
                motivo
            )
        )
    }

    // La nutrióloga modifica la fecha, hora o motivo.
    suspend fun actualizarCita(
        idCita: String,
        fecha: String,
        hora: String,
        motivo: String?
    ) {
        supabase.from("citas").update({
            set("fecha", fecha)
            set("hora", hora)

            if (motivo != null) {
                set("motivo", motivo)
            }
        }) {
            filter {
                eq("id", idCita)
            }
        }
    }

    // La nutrióloga cancela o elimina una cita.
    suspend fun eliminarCita(idCita: String) {
        supabase.from("citas").delete {
            filter {
                eq("id", idCita)
            }
        }
    }

    // La nutrióloga cambia el estado de una cita.
    suspend fun cambiarEstadoCita(
        idCita: String,
        estado: String
    ) {
        supabase.from("citas").update({
            set("estado", estado)
        }) {
            filter {
                eq("id", idCita)
            }
        }
    }

    // ===============================================================
    // 4. DETALLE DE CONSULTA Y MEDICIONES
    // ===============================================================

    // Registra las mediciones de una consulta y marca
    // la cita como completada.
    suspend fun guardarConsulta(d: DetalleConsultaNuevo) {
        supabase.from("detalle_consulta").upsert(d) {
            onConflict = "id_cita"
        }

        cambiarEstadoCita(
            d.idCita,
            "completada"
        )
    }

    // Consulta las mediciones registradas en una cita.
    suspend fun detalleDeCita(
        idCita: String
    ): DetalleConsulta? =
        supabase.from("detalle_consulta").select {
            filter {
                eq("id_cita", idCita)
            }
        }.decodeList<DetalleConsulta>().firstOrNull()

    // ===============================================================
    // 5. OBSERVACIONES Y RECOMENDACIONES
    // ===============================================================

    // El paciente consulta sus observaciones.
    suspend fun misObservaciones(): List<Observacion> =
        supabase.from("observaciones").select {
            filter {
                eq("id_paciente", miId())
            }
            order("fecha", Order.DESCENDING)
        }.decodeList()

    // La nutrióloga registra una observación para un paciente.
    suspend fun escribirObservacion(
        idPaciente: String,
        texto: String,
        tipo: String?,
        idSeguimiento: String? = null
    ) {
        supabase.from("observaciones").insert(
            ObservacionNueva(
                miId(),
                idPaciente,
                idSeguimiento,
                texto,
                tipo
            )
        )
    }

    // La nutrióloga consulta las observaciones de un paciente.
    suspend fun observacionesDePaciente(
        idPaciente: String
    ): List<Observacion> =
        supabase.from("observaciones").select {
            filter {
                eq("id_paciente", idPaciente)
            }
            order("fecha", Order.DESCENDING)
        }.decodeList()

    // ===============================================================
    // 6. REPORTES DE PROGRESO
    // ===============================================================

    // El paciente consulta sus reportes.
    suspend fun misReportes(): List<ReporteProgreso> =
        supabase.from("reportes_progreso").select {
            filter {
                eq("id_paciente", miId())
            }
            order("fecha_generacion", Order.DESCENDING)
        }.decodeList()

    // Genera un reporte con los promedios del periodo y el IMC.
    suspend fun generarReporte(
        idPaciente: String,
        inicio: String,
        fin: String,
        analisis: String?
    ) {
        // Obtener los seguimientos del periodo.
        val dias = supabase.from("seguimiento_diario").select {
            filter {
                eq("id_paciente", idPaciente)
                gte("fecha", inicio)
                lte("fecha", fin)
            }
        }.decodeList<SeguimientoDiario>()

        // Obtener el expediente del paciente.
        val exp = expedienteDe(idPaciente)

        // Obtener el último peso registrado en una consulta.
        val ultimoPeso = supabase.from("detalle_consulta")
            .select(
                Columns.raw(
                    "peso, fecha_registro, citas!inner(id_paciente)"
                )
            ) {
                filter {
                    eq("citas.id_paciente", idPaciente)
                }
                order("fecha_registro", Order.DESCENDING)
                limit(1)
            }
            .decodeList<DetalleConsulta>()
            .firstOrNull()
            ?.peso ?: exp?.pesoInicial

        // Convertir la altura de centímetros a metros.
        val alturaM = exp?.altura?.div(100.0)

        // Calcular el IMC.
        val imc = if (
            ultimoPeso != null &&
            alturaM != null &&
            alturaM > 0
        ) {
            Math.round(
                (ultimoPeso / (alturaM * alturaM)) * 100
            ) / 100.0
        } else {
            null
        }

        // Clasificar el resultado del IMC.
        val estado = when {
            imc == null -> null
            imc < 18.5 -> "Bajo peso"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obesidad"
        }

        // Calcular el promedio de una lista.
        fun prom(valores: List<Double>): Double? =
            if (valores.isEmpty()) {
                null
            } else {
                Math.round(
                    valores.average() * 100
                ) / 100.0
            }

        // Guardar el reporte en Supabase.
        supabase.from("reportes_progreso").insert(
            ReporteProgreso(
                idPaciente = idPaciente,
                idNutriologa = miId(),
                imc = imc,
                estadoCorporal = estado,
                promedioAgua = prom(
                    dias.mapNotNull { it.consumoAgua }
                ),
                promedioSueno = prom(
                    dias.mapNotNull { it.horasSueno }
                ),
                promedioActividadFisica = prom(
                    dias.mapNotNull {
                        it.minutosActividad?.toDouble()
                    }
                ),
                analisis = analisis,
                periodoInicio = inicio,
                periodoFin = fin
            )
        )
    }
}