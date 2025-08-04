package com.universidad.prograv.proyecto_programacionv.Modelos

data class Usuarios(
    val uid : String = "",
    val nombre : String = "",
    val apellido : String = "",
    val correo : String = "",
    var role : String = ""
)