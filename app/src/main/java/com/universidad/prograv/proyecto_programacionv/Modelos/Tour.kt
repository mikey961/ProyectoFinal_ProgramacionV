package com.universidad.prograv.proyecto_programacionv.Modelos

import java.io.Serializable

data class Tour(
    val id : String? = null,
    val nombre : String? = null,
    val descripcion : String? = null,
    val duracion : String? = null,
    val cantidadVehiculos : Int? = null,
    val precioDoble : Double? = null,
    val precioIndividual : Double? = null,
    val horarios : List<String>? = null,
    val fecha : String? = null,
    val imagenUrl : String? = null,
    val tipoTour : String? = null
) : Serializable