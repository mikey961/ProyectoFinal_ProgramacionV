package com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.ReservasAdminAdapter
import com.universidad.prograv.proyecto_programacionv.Modelos.Reserva
import com.universidad.prograv.proyecto_programacionv.R

class AdminReservasFragment : Fragment() {
    private lateinit var rv_ReservasAdmin: RecyclerView
    private lateinit var actvFiltro: AutoCompleteTextView

    private var db = FirebaseFirestore.getInstance()

    private val reservasFull = mutableListOf<Reserva>()
    private val reservasVisibles = mutableListOf<Reserva>()
    private lateinit var adaptador: ReservasAdminAdapter

    private var filtroCorreoSeleccionado: String? = null
    private var listener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_reservas, container, false)

        rv_ReservasAdmin = view.findViewById(R.id.rv_AdminReservas)
        actvFiltro = view.findViewById(R.id.actv_FiltroClientes)

        rv_ReservasAdmin.layoutManager = LinearLayoutManager(requireContext())
        adaptador = ReservasAdminAdapter(
            reservasVisibles,
            onCancelarAdmin = { r -> onCancelarComoAdmin(r) },
            onRenovarAdmin = { r -> onRenovarComoAdmin(r) }
        )
        rv_ReservasAdmin.adapter = adaptador

        configurarFiltroUI()
        borrarReservasVencidasLocal()
        escucharReservas()

        return view
    }

    protected fun escucharReservas() {
        listener?.remove()
        listener = db.collection("reservas")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Toast.makeText(requireContext(), "Error: ${err.message}", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                reservasFull.clear()
                snap?.documents?.forEach { doc ->
                    doc.toObject(Reserva::class.java)
                        ?.let { reservasFull.add(it.copy(id = doc.id)) }
                }

                poblarOpcionesFiltro()
                aplicarFiltro()
            }
    }

    private fun poblarOpcionesFiltro() {
        val clientesUnicos: List<ClienteEtiqueta> = reservasFull
            .filter { !(it.correo.isNullOrBlank() && (it.nombre.isNullOrBlank() && it.apellido.isNullOrBlank())) }
            .groupBy { (it.correo ?: "${it.nombre ?: ""} ${it.apellido ?: ""}".trim()).lowercase() }
            .map { (_, lista) ->
                val r = lista.first()
                val correo = r.correo?.trim().orEmpty()
                val etiquetaNombre =
                    "${(r.nombre ?: "").trim()} ${(r.apellido ?: "").trim()}".trim()
                ClienteEtiqueta(
                    correo = if (correo.isNotEmpty()) correo else etiquetaNombre,
                    etiqueta = if (etiquetaNombre.isNotEmpty()) etiquetaNombre else correo
                )
            }
            .sortedBy { it.etiqueta.lowercase() }

        val opcionesUi = mutableListOf("Todos")
        opcionesUi += clientesUnicos.map { it.etiqueta }

        actvFiltro.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesUi)
        )

        actvFiltro.setTag(R.id.actv_FiltroClientes, clientesUnicos)


        if (filtroCorreoSeleccionado == null) {
            actvFiltro.setText("Todos", false)
        } else {
            val etiquetaSeleccionada = clientesUnicos.firstOrNull {
                it.correo.equals(filtroCorreoSeleccionado, ignoreCase = true)
            }?.etiqueta ?: "Todos"
            actvFiltro.setText(etiquetaSeleccionada, false)
        }
    }

    private fun configurarFiltroUI() {
        actvFiltro.setOnItemClickListener { _, _, _, _ ->
            val texto = actvFiltro.text?.toString()?.trim().orEmpty()
            if (texto.equals("Todos", ignoreCase = true)) {
                filtroCorreoSeleccionado = null
            } else {
                @Suppress("UNCHECKED_CAST")
                val clientesUnicos =
                    actvFiltro.getTag(R.id.actv_FiltroClientes) as? List<ClienteEtiqueta>
                val match = clientesUnicos?.firstOrNull { it.etiqueta.equals(texto, true) }
                filtroCorreoSeleccionado = match?.correo
            }
            aplicarFiltro()
        }
    }

    private fun aplicarFiltro() {
        reservasVisibles.clear()
        reservasVisibles += if (filtroCorreoSeleccionado.isNullOrEmpty()) {
            reservasFull
        } else {
            reservasFull.filter { r ->
                val clave = r.correo?.trim().ifNullOrBlank {
                    "${(r.nombre ?: "").trim()} ${(r.apellido ?: "").trim()}".trim()
                }
                clave.equals(filtroCorreoSeleccionado, ignoreCase = true)
            }
        }
        adaptador.notifyDataSetChanged()
    }

    private fun onCancelarComoAdmin(reserva: Reserva) {
        if (reserva.cancelRequested != true) {
            Toast.makeText(requireContext(), "El cliente no ha solicitado cancelar esta reserva.", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar cancelación")
            .setMessage("¿Desea cancelar esta reserva?")
            .setPositiveButton("Cancelar reserva") { _, _ ->
                val id = reserva.id ?: return@setPositiveButton

                val cal = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.HOUR_OF_DAY, 72)
                }
                val expiresAt = com.google.firebase.Timestamp(cal.time)
                db.collection("reservas").document(id)
                    .update(
                        mapOf(
                            "estado" to "cancelada",
                            "cancelRequested" to false,
                            "renewRequested" to false,
                            "cancelledAt" to FieldValue.serverTimestamp(),
                            "expiresAt" to expiresAt
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Reserva cancelada.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Atrás", null)
            .show()
    }

    private fun onRenovarComoAdmin(reserva: Reserva) {
        if (reserva.renewRequested != true) {
            Toast.makeText(requireContext(), "El cliente no ha solicitado renovar esta reserva.", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar renovación")
            .setMessage("¿Desea reactivar esta reserva?")
            .setPositiveButton("Renovar") { _, _ ->
                val id = reserva.id ?: return@setPositiveButton
                db.collection("reservas")
                    .document(id)
                    .update(
                        mapOf(
                            "estado" to "activa",
                            "renewRequested" to false,
                            "cancelRequested" to false,
                            "expiresAt" to null
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Reserva renovada.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Atrás", null)
            .show()
    }

    private fun borrarReservasVencidasLocal() {
        val ahora = com.google.firebase.Timestamp.now()
        db.collection("reservas")
            .whereEqualTo("estado", "cancelada")
            .whereLessThan("expiresAt", ahora)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) return@addOnSuccessListener
                val batch = db.batch()
                snap.documents.forEach { batch.delete(it.reference) }
                batch.commit()
            }
    }

    private inline fun String?.ifNullOrBlank(fallback: () -> String): String {
        return if (this.isNullOrBlank()) fallback() else this
    }

    private data class ClienteEtiqueta(val correo: String, val etiqueta: String)

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
        listener = null
    }
}