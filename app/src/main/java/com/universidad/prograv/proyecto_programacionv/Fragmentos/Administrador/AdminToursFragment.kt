package com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.Activities.Administrador.EditarTourActivity
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.TourAdapter
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminToursFragment : Fragment() {
    private lateinit var fabNuevoTour : FloatingActionButton
    private lateinit var db : FirebaseFirestore
    private lateinit var imgSeleccionada : ImageView
    private val SELECCIONAR_IMAGEN = 1001
    private var imagenUri : Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        val view = inflater.inflate(R.layout.fragment_admin_tours, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_Tours)

        db = FirebaseFirestore.getInstance()
        fabNuevoTour = view.findViewById(R.id.fab_NuevoTour)

        fabNuevoTour.setOnClickListener {
            mostrarBottomSheetCrearNuevoTour()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        eliminarToursVencidos{
            cargarTours(recyclerView)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECCIONAR_IMAGEN && resultCode == Activity.RESULT_OK){
            imagenUri = data?.data
            imgSeleccionada.setImageURI(imagenUri)
        }
    }

    private fun mostrarBottomSheetCrearNuevoTour(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_crear_nuevo_tour, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(bottomSheetView)

        val tipoTour = bottomSheetView.findViewById<AutoCompleteTextView>(R.id.actv_TipoTour)
        val layoutCuadraciclo = bottomSheetView.findViewById<View>(R.id.i_selectCuadracicloTour)
        val layoutCabalgata = bottomSheetView.findViewById<View>(R.id.i_selectHorasExtraCaballo)
        val layoutCaminata = bottomSheetView.findViewById<View>(R.id.i_selectHorasExtraCaminata)

        val cantidadVehiculosLayout = bottomSheetView.findViewById<TextInputLayout>(R.id.til_CantidadVehiculoPesado)
        val fechaTour = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_FechaTour)

        layoutCuadraciclo.visibility = View.GONE
        layoutCabalgata.visibility = View.GONE
        layoutCaminata.visibility = View.GONE

        val opcionesTour = listOf("Tour en cuadraciclo", "Tour cabalgata", "Caminata guiada")
        val adaptador = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesTour)
        tipoTour.setAdapter(adaptador)

        tipoTour.setOnItemClickListener { _, _, position, _ ->
            layoutCabalgata.visibility = View.GONE
            layoutCaminata.visibility = View.GONE

            layoutCuadraciclo.visibility = View.VISIBLE
            cantidadVehiculosLayout.visibility = View.VISIBLE

            when(opcionesTour[position]) {
                "Tour cabalgata" -> {
                    layoutCabalgata.visibility = View.VISIBLE
                    cantidadVehiculosLayout.hint = "Cantidad de caballos"
                }
                "Caminata guiada" -> {
                    layoutCaminata.visibility = View.VISIBLE
                    cantidadVehiculosLayout.visibility = View.GONE
                }
            }
        }

        val btnSeleccionarImagen = bottomSheetView.findViewById<Button>(R.id.button_SeleccionarImagen)
        imgSeleccionada = bottomSheetView.findViewById(R.id.iv_ImagenSeleccionada)

        btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, SELECCIONAR_IMAGEN)
        }

        fechaTour.setOnClickListener {
            val constraintBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccione la fecha del tour")
                .setCalendarConstraints(constraintBuilder.build())
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")
            datePicker.addOnPositiveButtonClickListener {
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                fechaTour.setText(fecha)
            }
        }

        val btnGuardar = bottomSheetView.findViewById<MaterialButton>(R.id.buttonGuardarTour)
        btnGuardar.setOnClickListener {
            guardarTour(bottomSheetView, dialog)
        }

        val btnCancelar = bottomSheetView.findViewById<MaterialButton>(R.id.buttonCancelar)
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun guardarTour(bottomSheetView : View, dialog: BottomSheetDialog){
        db = FirebaseFirestore.getInstance()

        val nombre = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_NombreTour).text?.toString()?.trim().orEmpty()
        val descripcion = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_DescriptionTour).text?.toString()?.trim().orEmpty()
        val duracion = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_DuracionTour).text?.toString()?.trim().orEmpty()
        val tipoTourSeleccionado = bottomSheetView.findViewById<AutoCompleteTextView>(R.id.actv_TipoTour).text.toString()

        val cantidadVehiculosEdit = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_CantidadVehiculosPesados)
        val cantidadVehiculos = cantidadVehiculosEdit?.text?.toString()?.trim()?.toIntOrNull()
        val layoutCuadraciclo = bottomSheetView.findViewById<View>(R.id.i_selectCuadracicloTour)
        val checkHoraMañana = layoutCuadraciclo.findViewById<CheckBox>(R.id.checkHoraMañana)
        val checkHoraTarde = layoutCuadraciclo.findViewById<CheckBox>(R.id.checkHoraTarde)

        val precioIndividual = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_TourPrecioIndividual).text?.toString()?.trim().orEmpty().toDoubleOrNull()
        val precioDoble = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_TourPrecioDoble).text?.toString()?.trim().orEmpty().toDoubleOrNull()
        val fecha = bottomSheetView.findViewById<TextInputEditText>(R.id.tiet_FechaTour).text?.toString()?.trim().orEmpty()

        val horarios = mutableListOf<String>()
        if (checkHoraMañana.isChecked) horarios.add(checkHoraMañana.text.toString())
        if (checkHoraTarde.isChecked) horarios.add(checkHoraTarde.text.toString())
        if (bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra1_Cabalgata).isChecked) horarios.add(bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra1_Cabalgata).text.toString())
        if (bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra2_Cabalgata).isChecked) horarios.add(bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra2_Cabalgata).text.toString())
        if (bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra1_Caminata).isChecked) horarios.add(bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra1_Caminata).text.toString())
        if (bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra2_Caminata).isChecked) horarios.add(bottomSheetView.findViewById<CheckBox>(R.id.checkBoxExtra2_Caminata).text.toString())

        if (nombre.isEmpty() || descripcion.isEmpty() || duracion.isEmpty() ||
            precioIndividual == null || precioDoble == null || fecha.isEmpty() || imagenUri == null || (tipoTourSeleccionado != "Caminata guiada" && cantidadVehiculos == null)) {
            Toast.makeText(requireContext(), "Por favor complete todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        if (horarios.isEmpty()) {
            Toast.makeText(requireContext(), "Debe seleccionar al menos un horario para el tour", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val fechaDate = try { sdf.parse(fecha) } catch (e : Exception){ null }

        if (fechaDate == null){
            Toast.makeText(requireContext(), "Fecha inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = Calendar.getInstance()
        cal.time = fechaDate
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val fechaTs = Timestamp(cal.time)


        subirImagenAPostImages(requireContext(), imagenUri!!){ urlImagen ->
            val nuevoTour = hashMapOf<String, Any>(
                "nombre" to nombre,
                "descripcion" to descripcion,
                "duracion" to duracion,
                "precioIndividual" to precioIndividual,
                "precioDoble" to precioDoble,
                "fecha" to fecha,
                "fechaTs" to fechaTs,
                "horarios" to horarios,
                "imagenUrl" to urlImagen,
                "tipoTour" to tipoTourSeleccionado,
                "creadoEn" to Timestamp.now()
            )

            if (tipoTourSeleccionado == "Tour cabalgata"){
                nuevoTour["cantidadVehiculos"] = cantidadVehiculos!!
            } else if (tipoTourSeleccionado == "Tour en cuadraciclo") {
                nuevoTour["cantidadVehiculos"] = cantidadVehiculos!!
            }

            db.collection("tours")
                .add(nuevoTour)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "¡Tour Guardado!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    eliminarToursVencidos{
                        view?.findViewById<RecyclerView>(R.id.recycler_Tours)?.let {
                            cargarTours(it)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al guardar el tour: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun eliminarToursVencidos(onComplete : (() -> Unit)? = null){
        val ahora = Timestamp.now()

        db.collection("tours")
            .whereLessThan("fechaTs", ahora)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty){
                    onComplete?.invoke()
                    return@addOnSuccessListener
                }

                val docs = snap.documents
                val chunkSize = 450
                var pendientes = 0
                if (docs.isEmpty()){
                    onComplete?.invoke()
                    return@addOnSuccessListener
                }
                val chunks = docs.chunked(chunkSize)
                pendientes = chunks.size

                chunks.forEach { group ->
                    val batch = db.batch()
                    group.forEach { d -> batch.delete(d.reference) }
                    batch.commit()
                        .addOnCompleteListener {
                            pendientes--
                            if (pendientes == 0) onComplete?.invoke()
                        }
                }
            }
            .addOnFailureListener {
                onComplete?.invoke()
            }
    }

    private fun subirImagenAPostImages(context : Context, uri : Uri, onSuccess : (String) -> Unit){
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

        val requestBody = FormBody.Builder()
            .add("key", "14c055acdc76d035caf28bb1ee03372d")
            .add("image", base64Image)
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        val cliente = OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        cliente.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error al subir imagen: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                try {
                    val json = JSONObject(responseBody)
                    val imageUrl = json.getJSONObject("data").getString("url")
                    requireActivity().runOnUiThread {
                        onSuccess(imageUrl)
                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error al parsear respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun cargarTours(recyclerView : RecyclerView){
        db.collection("tours")
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Map<String, Any>>()
                for (doc in result){
                    val data = doc.data.toMutableMap()
                    data["id"] = doc.id
                    lista.add(data)
                }

                val adaptador = TourAdapter(
                    requireContext(),
                    lista,
                    onVerMas = { tour ->
                        Toast.makeText(requireContext(), "Tour: ${tour["nombre"]}", Toast.LENGTH_SHORT).show()
                    },
                    onEditar = { tour ->
                        val intent = Intent(requireContext(), EditarTourActivity::class.java).apply {
                            putExtra("id", tour["id"] as? String)
                            putExtra("imagenUrl", tour["imagenUrl"] as? String)
                            putExtra("nombre", tour["nombre"] as? String)
                            putExtra("descripcion", tour["descripcion"] as? String)
                            putExtra("duracion", tour["duracion"] as? String)
                            putExtra("fecha", tour["fecha"] as? String)
                            putExtra("tipoTour", tour["tipoTour"] as? String)

                            (tour["precioIndividual"] as? Number)?.toDouble()?.let { putExtra("precioIndividual", it) }
                            (tour["precioDoble"] as? Number)?.toDouble()?.let { putExtra("precioDoble", it) }
                            (tour["cantidadCaballos"] as? Number)?.let { putExtra("cantidadCaballos", it.toInt()) }
                            (tour["cantidadVehiculos"] as? Number)?.let { putExtra("cantidadVehiculos", it.toInt()) }

                            val horarios = tour["horarios"] as? List<*>
                            putExtra("horarios", ArrayList(horarios?.mapNotNull { it?.toString() }))
                        }
                        startActivity(intent)
                        Toast.makeText(requireContext(), "Editar: ${tour["nombre"]}", Toast.LENGTH_SHORT).show()
                    },
                    onEliminar = { tour ->
                        val id = tour["id"] as? String ?: return@TourAdapter
                        db.collection("tours").document(id).delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Tour eliminado", Toast.LENGTH_SHORT).show()
                                eliminarToursVencidos{ cargarTours(recyclerView) }
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al eliminar tour", Toast.LENGTH_SHORT).show()
                            }
                    }
                )

                recyclerView.adapter = adaptador
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar tours", Toast.LENGTH_SHORT).show()
            }
    }
}