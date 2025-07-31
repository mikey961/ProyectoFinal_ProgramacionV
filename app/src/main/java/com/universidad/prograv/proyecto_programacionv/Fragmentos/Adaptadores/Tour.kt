package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

data class Tour(
    val id : String = "",
    val nombre : String = "",
    val descripcion : String = "",
    val duracion : String = "",
    val fecha : String = "",
    val horarios : List<String> = emptyList(),
    val cantidadVehiculos : Int = 0,
    val precioDoble : Double = 0.0,
    val precioIndividual : Double = 0.0,
    val imagenUrl : String = "",
    val tipoTour : String = ""
)