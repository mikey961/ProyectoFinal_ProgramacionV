package com.universidad.prograv.proyecto_programacionv.Activities.Cliente

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R

class VerTourActivity : AppCompatActivity() {
    private lateinit var imagen : ImageView
    private lateinit var nombre : TextView
    private lateinit var descripcion : TextView
    private lateinit var duracion : TextView
    private lateinit var cantidadVehiculos : TextView
    private lateinit var precioDoble : TextView
    private lateinit var precioIndividual : TextView
    private lateinit var fecha : TextView
    private lateinit var horarios : TextView
    private lateinit var puntajeEstrella : TextView
    private lateinit var btnReservar : Button
    private lateinit var btnVolver : Button
    private lateinit var labelCantidadVehiculos : TextView
    private lateinit var estrellas : List<ImageView>

    private var puntaje = 0.0f
    private var paso = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_tour)

        imagen = findViewById(R.id.iv_ImagenTour_VerTour)
        nombre = findViewById(R.id.tv_NombreTour_VerTour)
        descripcion = findViewById(R.id.tv_DescripcionTour_VerTour)
        duracion = findViewById(R.id.tv_DuracionTour_VerTour)
        cantidadVehiculos = findViewById(R.id.tv_CantidadVehiculos_VerTour)
        precioDoble = findViewById(R.id.tv_PrecioDoble_VerTour)
        precioIndividual = findViewById(R.id.tv_PrecionIndividual_VerTour)
        fecha = findViewById(R.id.tv_FechasTourDisponible_VerTour)
        horarios = findViewById(R.id.tv_HorariosDisponibles_VerTour)
        puntajeEstrella = findViewById(R.id.tv_PuntajeEstrella)
        btnReservar = findViewById(R.id.buttonReservar_VerTour)
        btnVolver = findViewById(R.id.buttonVolverTour)
        labelCantidadVehiculos = findViewById(R.id.tv_LabelCantidadVehiculos_VerTour)

        estrellas = listOf(
            findViewById(R.id.estrella1),
            findViewById(R.id.estrella2),
            findViewById(R.id.estrella3),
            findViewById(R.id.estrella4),
            findViewById(R.id.estrella5)
        )

        val tour = intent.getSerializableExtra("tour") as? Tour

        if (tour != null){
            Glide.with(this).load(tour.imagenUrl).into(imagen)
            nombre.text = tour.nombre
            descripcion.text = tour.descripcion
            duracion.text = tour.duracion

            if (tour.tipoTour?.equals("Tour cabalgata", ignoreCase = true) == true) {
                labelCantidadVehiculos.text = "Cantidad de caballos"
                cantidadVehiculos.text = tour.cantidadVehiculos?.toString() ?: "0"
            } else if (tour.tipoTour == "Caminata guiada") {
                labelCantidadVehiculos.visibility = TextView.GONE
                cantidadVehiculos.visibility = TextView.GONE
            } else {
                cantidadVehiculos.text = tour.cantidadVehiculos?.toString() ?: "0"
            }

            precioDoble.text = "$${tour.precioDoble?.toInt()}"
            precioIndividual.text = "$${tour.precioIndividual?.toInt()}"

            val date = tour.fecha ?: ""
            fecha.text = date

            horarios.text = tour.horarios?.joinToString(", ") ?: "Sin horarios"
        } else {
            Toast.makeText(this, "No se pudieron cargar los datos del tour", Toast.LENGTH_SHORT).show()
            finish()
        }

        estrellas.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val base = (index + 1).toFloat() - 0.5f
                puntaje = when (puntaje){
                    base -> base + 0.5f
                    base + 0.5f -> base
                    else -> base
                }
                if (puntaje > 5f) puntaje = 0.5f
                actualizarEstrellas(puntaje)
            }
        }

        btnReservar.setOnClickListener {
            Toast.makeText(this, "Ir a reservar tour...", Toast.LENGTH_SHORT).show()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun actualizarEstrellas(puntaje : Float){
        for (i in estrellas.indices){
            val estrella = estrellas[i]
            val posicion = i + 1

            when {
                puntaje >= posicion-> estrella.setImageResource(R.drawable.star)
                puntaje >= posicion - 0.5 -> estrella.setImageResource(R.drawable.star_half)
                else -> estrella.setImageResource(R.drawable.star_empty)
            }
        }
        puntajeEstrella.text = String.format("%.1f", puntaje)
    }
}