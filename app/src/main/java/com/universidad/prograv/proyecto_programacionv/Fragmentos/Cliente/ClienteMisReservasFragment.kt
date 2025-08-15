package com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.MisReservasAdapter
import com.universidad.prograv.proyecto_programacionv.Modelos.Reserva
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R

class ClienteMisReservasFragment : Fragment() {
    private lateinit var rv_Reservas : RecyclerView
    private lateinit var tv_Empty : TextView

    private var db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    private lateinit var adaptador : MisReservasAdapter
    private var reservas = mutableListOf<Reserva>()

    private var toursById : Map<String, Tour> = emptyMap()
    private var reservasListener : ListenerRegistration? = null
    private var toursListener : ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cliente_mis_reservas, container, false)

        rv_Reservas = view.findViewById(R.id.rv_MisReservas)
        tv_Empty = view.findViewById(R.id.tv_EmptyMisReservas)

        rv_Reservas.layoutManager = LinearLayoutManager(requireContext())
        adaptador = MisReservasAdapter(
            reservas,
            toursById,
            onCancelarClick = { r -> onCancelarClickCliente(r) },
            onRenovarClick = { r -> onRenovarClickCliente(r) },
            onGenerarDetalle = { r -> onGenerarDetalleReserva(r) }
        )
        rv_Reservas.adapter = adaptador

        escucharTours()
        escuhcarMisReservas()

        return view
    }

    private fun escucharTours(){
        toursListener = db.collection("tours")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener

                val map = mutableMapOf<String, Tour>()
                for (doc in snap.documents) {
                    val t = doc.toObject(Tour::class.java) ?: continue
                    val id = doc.id
                    map[id] = t.copy(id = id)
                }
                toursById = map
                adaptador.updateTours(toursById)
            }
    }

    private fun escuhcarMisReservas(){
        val correo = auth.currentUser?.email
        val query = if (!correo.isNullOrEmpty()) {
            db.collection("reservas").whereEqualTo("correo", correo)
        } else {
            db.collection("reservas")
        }

        reservasListener = query.addSnapshotListener { snap, err ->
            if (err != null) {
                Toast.makeText(requireContext(), "Error al cargar reservas", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            reservas.clear()
            if (snap != null && !snap.isEmpty) {
                for (doc in snap.documents) {
                    val r = doc.toObject(Reserva::class.java) ?: continue
                    reservas.add(r.copy(id = doc.id))
                }
            }

            adaptador.notifyDataSetChanged()
            tv_Empty.visibility = if (reservas.isEmpty()) View.VISIBLE else View.GONE
            rv_Reservas.visibility = if (reservas.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun onCancelarClickCliente(reserva : Reserva) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancelar reserva")
            .setMessage("¿Desea solicitar la cancelacion de esta reserva?")
            .setPositiveButton("Solicitar"){ _, _ ->
                val id = reserva.id ?: return@setPositiveButton
                val correo = reserva.correo ?: FirebaseAuth.getInstance().currentUser?.email.orEmpty()

                FirebaseFirestore.getInstance()
                    .collection("reservas")
                    .document(id)
                    .update(
                        mapOf(
                            "estado" to "cancelacion_pendiente",
                            "cancelRequested" to true,
                            "cancelRequestedBy" to (correo ?: ""),
                            "cancelRequestedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener{
                        Toast.makeText(requireContext(), "Solicitud enviada.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al solicitar cancelacion de reserva.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun onRenovarClickCliente(reserva: Reserva) {
        if (!reserva.estado.equals("cancelada", true)) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Renovar reserva")
            .setMessage("¿Desea solicitar la renovación de esta reserva?")
            .setPositiveButton("Solicitar") { _, _ ->
                val id = reserva.id ?: return@setPositiveButton
                FirebaseFirestore.getInstance()
                    .collection("reservas")
                    .document(id)
                    .update(
                        mapOf(
                            "renewRequested" to true,
                            "cancelRequested" to false,
                            "estado" to "renovacion_pendiente"
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Solicitud de renovación enviada.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun onGenerarDetalleReserva(reserva : Reserva) {}

    private fun generarQR(texto: String, size: Int): android.graphics.Bitmap {
        val matrix = com.google.zxing.MultiFormatWriter().encode(
            texto, com.google.zxing.BarcodeFormat.QR_CODE, size, size
        )
        val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }

    override fun onDestroy() {
        super.onDestroy()
        reservasListener?.remove()
        toursListener?.remove()
        reservasListener = null
        toursListener = null
    }
}