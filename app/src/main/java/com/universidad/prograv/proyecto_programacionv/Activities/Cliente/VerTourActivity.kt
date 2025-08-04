package com.universidad.prograv.proyecto_programacionv.Activities.Cliente

import android.os.Bundle
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
    private lateinit var btnReservar : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_tour)

        imagen = findViewById(R.id.iv_ImagenTour_VerTour)
        nombre = findViewById(R.id.tv_NombreTour_VerTour)
        descripcion = findViewById(R.id.tv_DescripcionTour_VerTour)
        duracion = findViewById(R.id.tv_DuracionTour_VerTour)
        cantidadVehiculos = findViewById(R.id.tv_CantidadVehiculos_VerTour)
        precioDoble = findViewById(R.id.tv_PrecioDoble_VerTour)
        precioIndividual = findViewById(R.id.tv_PrecioIndividual_VerTour)
        btnReservar = findViewById(R.id.buttonReservar_VerTour)

        val tour = intent.getSerializableExtra("tour") as? Tour

        if (tour != null) {
            Glide.with(this).load(tour.imagenUrl).into(imagen)
            nombre.text = tour.nombre
            descripcion.text = tour.descripcion
            duracion.text = "Duración: ${tour.duracion}"
            cantidadVehiculos.text = "Cantidad de vehículos: ${tour.cantidadVehiculos}"
            precioDoble.text = "Precio Adulto: ₡${tour.precioDoble}"
            precioIndividual.text = "Precio Menor: ₡${tour.precioIndividual}"
        } else {
            Toast.makeText(this, "No se pudieron cargar los datos del tour", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnReservar.setOnClickListener {
            Toast.makeText(this, "Ir a reservar el tour...", Toast.LENGTH_SHORT).show()
        }

    }
}