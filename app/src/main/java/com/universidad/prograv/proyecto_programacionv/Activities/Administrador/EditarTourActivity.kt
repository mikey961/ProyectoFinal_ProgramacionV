package com.universidad.prograv.proyecto_programacionv.Activities.Administrador

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditarTourActivity : AppCompatActivity() {
    private lateinit var imagenCabecera : ImageView
    private lateinit var nombreTour : TextInputEditText
    private lateinit var descripcionTour : TextInputEditText
    private lateinit var duracionTour : TextInputEditText
    private lateinit var cantidadVehiculos : TextInputEditText
    private lateinit var cantidadVehiculosLayout : TextInputLayout
    private lateinit var precioIndividual : TextInputEditText
    private lateinit var precioDoble : TextInputEditText
    private lateinit var fechaTour : TextInputEditText
    private lateinit var horasTour : MaterialAutoCompleteTextView
    private lateinit var btnModificar : MaterialButton
    private lateinit var btnVolver : MaterialButton
    private lateinit var tipoTourTitulo : TextView

    private lateinit var db : FirebaseFirestore
    private var tourId : String? = null
    private var imagenUrl : String? = null
    private var imagenUriNueva : Uri? = null
    private var modoEdicion = false

    private var SELECT_IMAGE = 101
    private var API_KEY_IMGBB = "14c055acdc76d035caf28bb1ee03372d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_tour)

        imagenCabecera = findViewById(R.id.iv_ImagenCabeceraTour)
        nombreTour = findViewById(R.id.tiet_NombreTourEditar)
        descripcionTour = findViewById(R.id.tiet_DescriptionTourEditar)
        duracionTour = findViewById(R.id.tiet_DuracionTourEditar)
        cantidadVehiculos = findViewById(R.id.tiet_CantidadVehiculosPesadosEditar)
        cantidadVehiculosLayout = findViewById(R.id.til_CantidadVehiculoPesadosEditar)
        precioIndividual = findViewById(R.id.tiet_TourPrecioIndividualEditar)
        precioDoble = findViewById(R.id.tiet_TourPrecioDobleEditar)
        fechaTour = findViewById(R.id.tiet_FechaTourEditar)
        horasTour = findViewById(R.id.mactv_HorasTourEditar)
        btnModificar = findViewById(R.id.buttonModificar)
        btnVolver = findViewById(R.id.buttonVolver)
        tipoTourTitulo = findViewById(R.id.tv_TipoTour)

        db = FirebaseFirestore.getInstance()

        tourId = intent.getStringExtra("id")
        nombreTour.setText(intent.getStringExtra("nombre"))
        descripcionTour.setText(intent.getStringExtra("descripcion"))
        duracionTour.setText(intent.getStringExtra("duracion"))
        cantidadVehiculos.setText(intent.getIntExtra("cantidadVehiculos", 0).toString())
        precioIndividual.setText(intent.getDoubleExtra("precioIndividual", 0.0).toString())
        precioDoble.setText(intent.getDoubleExtra("precioDoble", 0.0).toString())
        fechaTour.setText(intent.getStringExtra("fecha"))

        val tipoTour = intent.getStringExtra("tipoTour") ?: ""
        tipoTourTitulo.text = tipoTour

        val cantidadVehiculosBD = intent.getIntExtra("cantidadVehiculos", 0)
        val cantidadCaballosBD = intent.getIntExtra("cantidadCaballos", 0)

        when (tipoTour.lowercase(Locale.getDefault())) {
            "tour cabalgata" -> {
                cantidadVehiculosLayout.hint = "Cantidad de caballos"
                cantidadVehiculos.setText(cantidadCaballosBD.toString())
            }
            "caminata guiada" -> {
                cantidadVehiculosLayout.visibility = View.GONE
            }
            else -> {
                cantidadVehiculos.setText(cantidadVehiculosBD.toString())
            }
        }

        val horarios = intent.getStringArrayListExtra("horarios")
        horasTour.setText(horarios?.joinToString(", "))

        imagenUrl = intent.getStringExtra("imagenUrl")
        Glide.with(this).load(imagenUrl).into(imagenCabecera)

        configuracionHorasAutocomplete()

        imagenCabecera.setOnClickListener {
            if (modoEdicion){
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                startActivityForResult(intent, SELECT_IMAGE)
            }
        }

        btnModificar.setOnClickListener {
            if (!modoEdicion) {
                habilitarCampos(true)
                btnModificar.text = "Editar tour"
                modoEdicion = true
            } else {
                actualizarTour()
            }
        }

        btnVolver.setOnClickListener {
            finish()
        }

        fechaTour.setOnClickListener {
            if (modoEdicion){
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Seleccione la fecha del tour")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(
                        CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.now())
                            .build()
                    )
                    .build()

                picker.show(supportFragmentManager, "DATE_PICKER")
                picker.addOnPositiveButtonClickListener {
                    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    fechaTour.setText(formato.format(Date(it)))
                }
            }
        }

        habilitarCampos(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null){
            imagenUriNueva = data.data
            imagenCabecera.setImageURI(imagenUriNueva)

            actualizarImagenaImgBB(imagenUriNueva!!)
        }
    }

    private fun habilitarCampos(habilitar : Boolean){
        nombreTour.isEnabled = habilitar
        descripcionTour.isEnabled = habilitar
        duracionTour.isEnabled = habilitar
        cantidadVehiculos.isEnabled = habilitar
        precioIndividual.isEnabled = habilitar
        precioDoble.isEnabled = habilitar
        fechaTour.isEnabled = habilitar
        horasTour.isEnabled = habilitar

    }

    private fun configuracionHorasAutocomplete(){
        val horas = mutableListOf<String>()
        for (h in 8..15) {
            horas.add(String.format("%02d:00", h))
            horas.add(String.format("%02d:30", h))
        }
        horas.add("16:00")

        val seleccionadas = mutableSetOf<String>()

        val adaptador = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, horas)
        horasTour.setAdapter(adaptador)

        horasTour.setOnItemClickListener { parent, _, position, _ ->
            if (!modoEdicion) return@setOnItemClickListener

            val horaSeleccionada = parent.getItemAtPosition(position).toString()
            if (seleccionadas.add(horaSeleccionada)) {
                horasTour.setText(seleccionadas.joinToString(", "), false)
            } else {
                Toast.makeText(this, "Hora ya seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarTour(){
        if (tourId == null){
            Toast.makeText(this, "ID del tour no valido!", Toast.LENGTH_SHORT).show()
            return
        }

        val tourActualizado = mapOf(
            "nombre" to nombreTour.text.toString(),
            "descripcion" to descripcionTour.text.toString(),
            "duracion" to duracionTour.text.toString(),
            "cantidadVehiculos" to cantidadVehiculos.text.toString().toIntOrNull(),
            "precioIndividual" to precioIndividual.text.toString().toDoubleOrNull(),
            "precioDoble" to precioDoble.text.toString().toDoubleOrNull(),
            "fecha" to fechaTour.text.toString(),
            "horarios" to horasTour.text.toString().split(",").map { it.trim() },
            "imagenUrl" to imagenUrl

        )

        db.collection("tours")
            .document(tourId!!)
            .update(tourActualizado)
            .addOnSuccessListener {
                Toast.makeText(this, "Tour actualizado", Toast.LENGTH_SHORT).show()
                habilitarCampos(false)
                btnModificar.text = "Modificar"
                modoEdicion = false
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarImagenaImgBB(imageUri : Uri){
        val inputStream = contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val requestBody = FormBody.Builder()
            .add("key", API_KEY_IMGBB)
            .add("image", base64Image)
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        val cliente = OkHttpClient()

        cliente.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    Toast.makeText(this@EditarTourActivity, "Error subiendo imagen", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val imageUrl = json.getJSONObject("data").getString("url")

                runOnUiThread{
                    imagenUrl = imageUrl
                    Glide.with(this@EditarTourActivity).load(imagenUrl).into(imagenCabecera)
                    Toast.makeText(this@EditarTourActivity, "Imagen subida", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}