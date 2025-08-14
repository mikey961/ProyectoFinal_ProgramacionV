package com.universidad.prograv.proyecto_programacionv.Activities.Cliente

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

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

    private lateinit var tilHora : TextInputLayout
    private lateinit var actvHora : MaterialAutoCompleteTextView

    private lateinit var tv_SubTotalIndividual : TextView
    private lateinit var tv_SubTotalDoble : TextView
    private lateinit var tvTotal : TextView

    private lateinit var btnReservar : MaterialButton
    private lateinit var btnVolver : MaterialButton

    private lateinit var tour : Tour
    private val db = FirebaseFirestore.getInstance()

    private var selectedInd = 0
    private var selectedDob = 0
    private val maxPersonas = 10

    private var precioInd = 0.0
    private var precioDob = 0.0

    private val currency : NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale.US)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_cliente)

        tour = intent.getSerializableExtra("tour") as Tour
        precioInd = tour.precioIndividual ?: 0.0
        precioDob = tour.precioDoble ?: 0.0

        nombre = findViewById(R.id.tiet_NombreReserva)
        apellido = findViewById(R.id.tiet_ApellidoReserva)
        correo = findViewById(R.id.tiet_CorreoReserva)

        cbIndividual = findViewById(R.id.cbPrecioIndividual)
        cbDoble = findViewById(R.id.cbPrecioDoble)

        tilCantidadIndividual = findViewById(R.id.til_CantidadIndividual_Reservar)
        tilCantidadDoble = findViewById(R.id.til_cantidadDoble_Reservar)
        actv_CantidadIndividual = findViewById(R.id.actv_CantidadIndividual)
        actv_CantidadDoble = findViewById(R.id.actv_CantidadDoble)

        tilHora = findViewById(R.id.til_Hora_Reserva)
        actvHora = findViewById(R.id.actv_Hora_Reserva)

        tv_SubTotalIndividual = findViewById(R.id.tv_SubtotalIndividual)
        tv_SubTotalDoble = findViewById(R.id.tv_SubtotalDoble)
        tvTotal = findViewById(R.id.tv_TotalReserva)

        btnReservar = findViewById(R.id.buttonResrvarTour)
        btnVolver = findViewById(R.id.buttonVolver_Reserva)

        setHorasAdapter()
        setCantidadAdapaters(indMax = maxPersonas, dobMax = maxPersonas)

        tilCantidadIndividual.visibility = View.GONE
        tilCantidadDoble.visibility = View.GONE

        cbIndividual.setOnCheckedChangeListener{ _, checked ->
            tilCantidadIndividual.visibility = if (checked) View.VISIBLE else View.GONE
            if (!checked){
                selectedInd = 0
                actv_CantidadIndividual.setText("")
            }
            recomputarOpcionesYTotal()
        }

        cbDoble.setOnCheckedChangeListener{ _, checked ->
            tilCantidadDoble.visibility = if (checked) View.VISIBLE else View.GONE
            if (!checked){
                selectedDob = 0
                actv_CantidadDoble.setText("")
            }
            recomputarOpcionesYTotal()
        }

        actv_CantidadIndividual.setOnItemClickListener{ _, _, position, _ ->
            val opciones = opcionesIndividual(maxDisponibleIndividual())
            selectedInd = opciones[position].toInt()
            limitarPorMaximo()
            recomputarOpcionesYTotal()
        }

        actv_CantidadDoble.setOnItemClickListener{ _, _, position, _ ->
            val opciones = opcionesDoble(maxDisponibleDoble())
            selectedDob = opciones[position].toInt()
            limitarPorMaximo()
            recomputarOpcionesYTotal()
        }

        btnReservar.setOnClickListener {
            onReservar()
        }

        btnVolver.setOnClickListener {
            finish()
        }

        actualizarTotal()
    }

    private fun setHorasAdapter(){
        val horas = (tour.horarios ?: emptyList()).ifEmpty { listOf("Sin horarios") }
        actvHora.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, horas))
        actvHora.setText("", false)
        tilHora.visibility = if (horas.first() == "Sin horarios") View.GONE else View.VISIBLE
    }

    private fun setCantidadAdapaters(indMax : Int, dobMax : Int){
        actv_CantidadIndividual.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesIndividual(indMax)))
        actv_CantidadDoble.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDoble(dobMax)))
    }

    private fun opcionesIndividual(max : Int) : List<String> = (1..max).map { it.toString() }

    private fun opcionesDoble(max : Int) : List<String> = (2..max step 2).map { it.toString() }

    private fun maxDisponibleIndividual() : Int {
        val restante = maxPersonas - selectedDob
        return max(0, restante).coerceAtLeast(1)
    }

    private fun maxDisponibleDoble() : Int {
        val restante = maxPersonas - selectedInd
        return max(0, restante)
    }

    private fun limitarPorMaximo(){
        val total = selectedInd + selectedDob
        if (total > maxPersonas){
            if (cbDoble.isChecked && selectedDob > 0){
                val permitido = max(0, maxPersonas - selectedInd)
                val nuevoDob = permitido - (permitido % 2)
                if (nuevoDob != selectedDob){
                    selectedDob = nuevoDob
                    actv_CantidadDoble.setText(if (nuevoDob == 0) "" else nuevoDob.toString(), false)
                    Toast.makeText(this, "Maximo $maxPersonas personas en total.", Toast.LENGTH_SHORT).show()
                }
            } else if (cbIndividual.isChecked && selectedInd > 0){
                val permitido = max(0, maxPersonas - selectedDob)
                if (permitido != selectedInd){
                    selectedInd = permitido
                    actv_CantidadIndividual.setText(if (permitido == 0) "" else permitido.toString(), false)
                    Toast.makeText(this, "Maximo $maxPersonas personas en total.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun recomputarOpcionesYTotal(){
        val indMax = maxDisponibleIndividual().coerceAtLeast(1)
        val dobMax = maxDisponibleDoble().coerceAtMost(maxPersonas)

        actv_CantidadIndividual.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesIndividual(indMax)))

        val dobMaxPar = if (dobMax < 2) 0 else dobMax - (dobMax % 2)
        actv_CantidadDoble.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDoble(dobMaxPar)))

        actualizarTotal()
    }

    private fun actualizarTotal(){
        val precioInd = tour.precioIndividual ?: 0.0
        val precioDob = tour.precioDoble ?: 0.0

        val cantInd = selectedInd
        val cantDobPersonas = selectedDob
        val paresDobles = cantDobPersonas / 2

        val subtotalInd = precioInd * cantInd
        val subtotalDob = precioDob * paresDobles
        val total = subtotalInd + subtotalDob

        tv_SubTotalIndividual.text = "Individual: ${currency.format(subtotalInd)}"
        tv_SubTotalDoble.text = "Doble: ${currency.format(subtotalDob)}"
        tvTotal.text = "Total: ${currency.format(total)}"
    }

    private fun onReservar(){
        val nombreStr = nombre.text?.toString()?.trim().orEmpty()
        val apellidoStr = apellido.text?.toString()?.trim().orEmpty()
        val correoStr = correo.text?.toString()?.trim().orEmpty()
        val horaElegida = actvHora.text?.toString()?.trim().orEmpty()

        if (nombreStr.isEmpty() || apellidoStr.isEmpty() || correoStr.isEmpty()){
            Toast.makeText(this, "Complete nombre, apellido y correo.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!cbIndividual.isChecked && !cbDoble.isChecked){
            Toast.makeText(this, "Seleccione al menos un tipo de precio", Toast.LENGTH_SHORT).show()
            return
        }

        if (cbIndividual.isChecked && selectedInd == 0){
            Toast.makeText(this, "Seleccione la cantidad de personas con precio individual", Toast.LENGTH_SHORT).show()
            return
        }

        if (cbDoble.isChecked && selectedDob == 0){
            Toast.makeText(this, "Seleccione la cantidad de personas con precio Doble", Toast.LENGTH_SHORT).show()
            return
        }

        if (tilHora.visibility == View.VISIBLE){
            if (horaElegida.isEmpty() || horaElegida.equals("Sin horarios", ignoreCase = true)){
                Toast.makeText(this, "Seleccione una hora", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val subTotalInd = if (cbIndividual.isChecked) precioInd * selectedInd else 0.0
        val subTotalDob = if (cbDoble.isChecked) precioDob * (selectedDob / 2) else 0.0
        val total = subTotalInd + subTotalDob

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = formatoFecha.format(Date())

        val data = hashMapOf(
            "idTour" to (tour.id ?: ""),
            "nombreTour" to (tour.nombre ?: ""),
            "nombre" to nombreStr,
            "apellido" to apellidoStr,
            "correo" to correoStr,
            "precioIndividual" to precioInd,
            "precioDoble" to precioDob,
            "cantidadIndividual" to selectedInd,
            "cantidadDoble" to selectedDob,
            "subTotalIndividual" to subTotalInd,
            "subTotalDoble" to subTotalDob,
            "total" to total,
            "fechaTour" to (tour.fecha ?: ""),
            "horaTour" to horaElegida,
            "fechaReserva" to  fechaActual
        )

        val slotDocId = slotId(tour.id ?: "", tour.fecha ?: "", horaElegida)
        val docRef = db.collection("reservas").document(slotDocId)

        db.runTransaction { tr ->
            val snap = tr.get(docRef)
            if (snap.exists()){
                throw FirebaseFirestoreException(
                    "Ya reservado",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }
            tr.set(docRef, data)
            null
        }.addOnFailureListener {
            Toast.makeText(this, "Reserva creada con exito!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            if (e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.ABORTED) {
                Toast.makeText(this, "Lo sentimos, este tour ya se encuentra reservado", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Error al crear la reserva: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun slotId(tourId : String, fecha : String, hora : String) : String {
        fun norm(s : String) = s
            .lowercase(Locale.ROOT)
            .replace("[^a-z0-9]+".toRegex(), "-")

        return "tour_${norm(tourId)}__fecha_${norm(fecha)}__hora_${norm(hora)}"
    }
}