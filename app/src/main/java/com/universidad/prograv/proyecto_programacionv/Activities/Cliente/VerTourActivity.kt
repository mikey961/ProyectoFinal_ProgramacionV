package com.universidad.prograv.proyecto_programacionv.Activities.Cliente

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R
import java.lang.reflect.Field

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

    private lateinit var ivFavorite : ImageView
    private var isFavorite = false

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var puntaje = 0.0f
    private var paso = 0.5f

    private var tourId : String = ""
    private var nombreTour : String = ""
    private var imagenUrl : String = ""
    private var precioInd : Double? = null

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

        ivFavorite = findViewById(R.id.iv_Favorite)

        val tour = intent.getSerializableExtra("tour") as? Tour
        if (tour != null){
            Glide.with(this).load(tour.imagenUrl).into(imagen)
            nombre.text = tour.nombre
            descripcion.text = tour.descripcion
            duracion.text = tour.duracion

            tourId = tour.id ?: ""
            nombreTour = tour.nombre ?: ""
            imagenUrl = tour.imagenUrl ?: ""
            precioInd = tour.precioIndividual

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

            btnReservar.setOnClickListener {
                val intent = Intent(this, DatosCliente::class.java)
                intent.putExtra("tour", tour)
                startActivity(intent)
            }

            cargarEstadoFavoritos()
            cargarMiRaiting()
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
                guardarRating(puntaje)
            }
        }

        ivFavorite.setOnClickListener { toggleFavorito() }

        btnVolver.setOnClickListener { finish() }
    }

    private fun actualizarEstrellas(puntaje : Float){
        for (i in estrellas.indices){
            val estrella = estrellas[i]
            val posicion = i + 1

            when {
                puntaje >= posicion -> estrella.setImageResource(R.drawable.star)
                puntaje >= posicion - 0.5 -> estrella.setImageResource(R.drawable.star_half)
                else -> estrella.setImageResource(R.drawable.star_empty)
            }
        }
        puntajeEstrella.text = String.format("%.1f", puntaje)
    }

    private fun cargarEstadoFavoritos(){
        val uid = uidOrNull() ?: return
        if (tourId.isEmpty()) return

        db.collection("favorites")
            .document(uid)
            .collection("tours")
            .document(tourId)
            .get()
            .addOnSuccessListener { doc ->
                isFavorite = doc.exists()
                applyFavoriteTint(isFavorite)
            }
    }

    private fun applyFavoriteTint(favorito : Boolean){
        ivFavorite.setImageResource(R.drawable.ic_favoritos)
        val colorRes = if (favorito) R.color.Rojo else R.color.grisFavorito
        ImageViewCompat.setImageTintList(
            ivFavorite,
            ColorStateList.valueOf(ContextCompat.getColor(this, colorRes))
        )
        ivFavorite.contentDescription = if (favorito) "Quitar de favoritos" else "Agregar a favoritos"
    }

    private fun toggleFavorito(){
        val uid  = uidOrNull() ?: run {
            Toast.makeText(this, "Debes iniciar sesion", Toast.LENGTH_SHORT).show()
            return
        }

        if (tourId.isEmpty()){
            Toast.makeText(this, "ID del tour invalido", Toast.LENGTH_SHORT).show()
        }

        val ref = db.collection("favorites")
            .document(uid)
            .collection("tours")
            .document(tourId)

        if (isFavorite){
            ref.delete()
                .addOnSuccessListener{
                    isFavorite = false
                    applyFavoriteTint(false)
                    Toast.makeText(this, "Quitado de favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            val data = hashMapOf(
                "tourId" to tourId,
                "nombreTour" to nombreTour,
                "imagenUrl" to imagenUrl,
                "precioIndividual" to (precioInd ?: 0.0),
                "createdAt" to FieldValue.serverTimestamp()
            )

            ref.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    isFavorite = true
                    applyFavoriteTint(true)
                    Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarMiRaiting(){
        val uid = uidOrNull() ?: return
        if (tourId.isEmpty()) return

        val docId = "${uid}_${tourId}"
        db.collection("ratings").document(docId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()){
                    val r = doc.getDouble("rating") ?: 0.0
                    puntaje = r.toFloat().coerceIn(0.5f, 5.0f)
                    actualizarEstrellas(puntaje)
                }
            }
    }

    private fun guardarRating(valor : Float){
        val uid = uidOrNull() ?: return
        if (tourId.isEmpty()) return

        val docId = "${uid}_${tourId}"
        val data = hashMapOf(
            "uid" to uid,
            "tourId" to tourId,
            "rating" to valor.toDouble(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("ratings").document(docId)
            .set(data, SetOptions.merge())
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar rating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uidOrNull() : String? = auth.currentUser?.uid
}