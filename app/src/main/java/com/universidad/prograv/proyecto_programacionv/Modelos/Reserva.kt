package com.universidad.prograv.proyecto_programacionv.Modelos

import java.io.Serializable

data class Reserva(
    val id : String = "",
    val idTour : String = "",
    val nombreTour : String = "",
    val nombre : String = "",
    val apellido : String = "",
    val correo : String = "",
    val fecha : String = "",
    val fechaTour : String = "",
    val horaTour : String = "",
    val estado: String? = "activa",
    val total: Double? = 0.0,
    val totalOriginal: Double? = 0.0,
    val aplicoWelcome50: Boolean? = false,
    val cancelRequested: Boolean? = false,
    val renewRequested: Boolean? = false,
    val cancelRequestedBy: String? = null,
    val cancelRequestedAt: com.google.firebase.Timestamp? = null
) : Serializable
