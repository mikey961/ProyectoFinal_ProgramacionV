package com.universidad.prograv.proyecto_programacionv.Activities.Cliente

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatosCliente : AppCompatActivity() {
    private lateinit var nombre : TextInputEditText
    private lateinit var apellido : TextInputEditText
    private lateinit var correo : TextInputEditText

    private lateinit var cbIndividual : MaterialCheckBox
    private lateinit var cbDoble : MaterialCheckBox

    private lateinit var tilCantidadIndividual : TextInputLayout
    private lateinit var tilCantidadDoble : TextInputLayout

    private lateinit var actv_CantidadIndividual : MaterialAutoCompleteTextView
    private lateinit var actv_CantidadDoble : MaterialAutoCompleteTextView

    private lateinit var btnReservar : MaterialButton
    private lateinit var btnCarrito : MaterialButton
    private lateinit var btnVolver : MaterialButton

    private lateinit var tour : Tour
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_cliente)

        tour = intent.getSerializableExtra("tour") as Tour

        nombre = findViewById(R.id.tiet_NombreReserva)
        apellido = findViewById(R.id.tiet_ApellidoReserva)
        correo = findViewById(R.id.tiet_CorreoReserva)

        cbIndividual = findViewById(R.id.cbPrecioIndividual)
        cbDoble = findViewById(R.id.cbPrecioDoble)

        tilCantidadIndividual = findViewById(R.id.til_CantidadIndividual_Reservar)
        tilCantidadDoble = findViewById(R.id.til_cantidadDoble_Reservar)

        actv_CantidadIndividual = findViewById(R.id.actv_CantidadIndividual)
        actv_CantidadDoble = findViewById(R.id.actv_CantidadDoble)

        btnReservar = findViewById(R.id.buttonResrvarTour)
        btnCarrito = findViewById(R.id.buttonEnviarACarritoTour)
        btnVolver = findViewById(R.id.buttonVolver_Reserva)

        tilCantidadIndividual.visibility = View.GONE
        tilCantidadDoble.visibility = View.GONE

        val cantidadIndividual = (1..10).map { it.toString() }
        val cantidadDoble = listOf("2", "4", "6", "8", "10")

        actv_CantidadIndividual.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cantidadIndividual))
        actv_CantidadDoble.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cantidadDoble))

        cbIndividual.setOnCheckedChangeListener { _, isChecked ->
            tilCantidadIndividual.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        cbDoble.setOnCheckedChangeListener { _, isChecked ->
            tilCantidadDoble.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnVolver.setOnClickListener {
            finish()
        }

        btnReservar.setOnClickListener {
            guardarReserva(enCarrito = false)
        }

        btnCarrito.setOnClickListener {
            guardarReserva(enCarrito = true)
        }
    }

    private fun guardarReserva(enCarrito : Boolean){
        val nombreCliente = nombre.text.toString().trim()
        val apellidoCliente = apellido.text.toString().trim()
        val correoCliente = correo.text.toString().trim()

        if (nombreCliente.isEmpty() || apellidoCliente.isEmpty() || correoCliente.isEmpty()){
            Toast.makeText(this, "Es obligatorio completar los datos personales", Toast.LENGTH_SHORT).show()
            return
        }

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaReserva = formatoFecha.format(Date())

        val reserva = mutableMapOf<String, Any>(
            "nombre" to nombreCliente,
            "apellido" to apellidoCliente,
            "correo" to correoCliente,
            "idTour" to tour.id!!,
            "nombreTour" to tour.nombre!!,
            "fechaReserva" to fechaReserva,
            "enCarrito" to enCarrito
        )

        var totalPersonas = 0

        if (cbIndividual.isChecked){
            val cantIndividual = actv_CantidadIndividual.text.toString().toIntOrNull()
            if (cantIndividual == null){
                Toast.makeText(this, "Seleccione cantidad de personas con precio individual", Toast.LENGTH_SHORT).show()
                return
            }
            reserva["cantidadIndividual"] = cantIndividual
            totalPersonas += cantIndividual
        }

        if (cbDoble.isChecked){
            val cantDoble = actv_CantidadDoble.text.toString().toIntOrNull()
            if (cantDoble == null){
                Toast.makeText(this, "Seleccione cantidad personas con precio doble", Toast.LENGTH_SHORT).show()
                return
            }
            reserva["cantidadDoble"] = cantDoble
            totalPersonas += cantDoble
        }

        if (!cbIndividual.isChecked && !cbDoble.isChecked){
            Toast.makeText(this, "Debe seleccionar al menos un tipo de precio", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("reservas")
            .add(reserva)
            .addOnSuccessListener {
                Toast.makeText(this, if (enCarrito) "Agregado al carrito" else "Reserva exitosa", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Error al guardar reserva", Toast.LENGTH_LONG).show()
            }
    }
}