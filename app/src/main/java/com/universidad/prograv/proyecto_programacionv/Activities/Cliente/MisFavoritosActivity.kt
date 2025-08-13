package com.universidad.prograv.proyecto_programacionv.Activities.Cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.FavoritosAdapter
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R

class MisFavoritosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnVolver : MaterialButton
    private lateinit var adapter: FavoritosAdapter

    private val listaFavoritos = mutableListOf<Tour>()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var favListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tours_favoritos)

        recyclerView = findViewById(R.id.recyclerFavoritos)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnVolver = findViewById(R.id.buttonVolverMiPerfil)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FavoritosAdapter(
            lista = listaFavoritos,
            onVerTour = { tour ->
                val intent = Intent(this, VerTourActivity::class.java)
                intent.putExtra("tour", tour)
                startActivity(intent)
            },
            onQuitarFavorito = { tour ->
                quitarDeFavoritos(tour)
            }
        )
        recyclerView.adapter = adapter

        btnVolver.setOnClickListener { finish() }
    }

    private fun observarFavoritos() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        favListener = db.collection("favorites")
            .document(uid)
            .collection("tours")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar favoritos: ${e.message}", Toast.LENGTH_SHORT).show()
                    mostrarVacio(true)
                    return@addSnapshotListener
                }

                val docs = snap?.documents.orEmpty()
                if (docs.isEmpty()) {
                    listaFavoritos.clear()
                    adapter.notifyDataSetChanged()
                    mostrarVacio(true)
                    return@addSnapshotListener
                }

                val basePorId = mutableMapOf<String, Tour>()
                val ids = mutableListOf<String>()

                docs.forEach { d ->
                    val tourId = d.getString("tourId") ?: return@forEach
                    val nombreTour = d.getString("nombreTour") ?: d.getString("nombre") ?: ""
                    val imagenUrl = d.getString("imagenUrl") ?: ""
                    val precioIndividual = d.getDouble("precioIndividual")

                    basePorId[tourId] = Tour(
                        id = tourId,
                        nombre = nombreTour,
                        imagenUrl = imagenUrl,
                        precioIndividual = precioIndividual
                    )
                    ids.add(tourId)
                }

                traerDetallesTours(ids) { detalles ->
                    listaFavoritos.clear()

                    ids.forEach { id ->
                        val base = basePorId[id]
                        val det = detalles[id]

                        val combinado = when {
                            base != null && det != null -> base.copy(
                                descripcion = det.descripcion ?: base.descripcion,
                                duracion = det.duracion ?: base.duracion,
                                cantidadVehiculos = det.cantidadVehiculos ?: base.cantidadVehiculos,
                                precioDoble = det.precioDoble ?: base.precioDoble,
                                horarios = det.horarios ?: base.horarios,
                                fecha = det.fecha ?: base.fecha,
                                tipoTour = det.tipoTour ?: base.tipoTour,
                                creadoEn = det.creadoEn ?: base.creadoEn
                            )
                            base != null -> base
                            else -> det
                        }

                        combinado?.let { listaFavoritos.add(it) }
                    }

                    adapter.notifyDataSetChanged()
                    mostrarVacio(listaFavoritos.isEmpty())
                }
            }
    }

    private fun traerDetallesTours(ids: List<String>, onDone: (Map<String, Tour>) -> Unit) {
        if (ids.isEmpty()) { onDone(emptyMap()); return }

        val chunks = ids.chunked(10)
        val resultados = mutableMapOf<String, Tour>()
        var pendientes = chunks.size

        chunks.forEach { group ->
            db.collection("tours")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), group)
                .get()
                .addOnSuccessListener { snap ->
                    snap.documents.forEach { doc ->
                        val t = doc.toObject(Tour::class.java)
                        val id = doc.id
                        if (t != null) {
                            resultados[id] = if (t.id.isNullOrEmpty()) t.copy(id = id) else t
                        }
                    }
                }
                .addOnCompleteListener {
                    pendientes--
                    if (pendientes == 0) onDone(resultados)
                }
        }
    }

    private fun quitarDeFavoritos(tour: Tour) {
        val uid = auth.currentUser?.uid ?: return
        val id = tour.id ?: return

        db.collection("favorites")
            .document(uid)
            .collection("tours")
            .document(id)
            .delete()
            .addOnSuccessListener {
                val idx = listaFavoritos.indexOfFirst { it.id == id }
                if (idx >= 0) {
                    listaFavoritos.removeAt(idx)
                    adapter.notifyItemRemoved(idx)
                    mostrarVacio(listaFavoritos.isEmpty())
                }
            }
    }

    private fun mostrarVacio(vacio: Boolean) {
        tvEmpty.visibility = if (vacio) View.VISIBLE else View.GONE
        recyclerView.visibility = if (vacio) View.GONE else View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        observarFavoritos()
    }

    override fun onStop() {
        super.onStop()
        favListener?.remove()
        favListener = null
    }
}