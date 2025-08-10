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
import com.google.firebase.auth.FirebaseAuth
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
        adaptador = MisReservasAdapter(reservas, toursById) {
            Toast.makeText(requireContext(), "Solicitud de cancelacion de reserva enviada", Toast.LENGTH_SHORT).show()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        reservasListener?.remove()
        toursListener?.remove()
        reservasListener = null
        toursListener = null
    }
}