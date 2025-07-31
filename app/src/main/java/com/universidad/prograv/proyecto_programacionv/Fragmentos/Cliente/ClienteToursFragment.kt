package com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.ClienteToursAdapter
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.Tour
import com.universidad.prograv.proyecto_programacionv.R


class ClienteToursFragment : Fragment() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var adaptador : ClienteToursAdapter
    private var tourList = mutableListOf<Tour>()
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cliente_tours, container, false)

        recyclerView = view.findViewById(R.id.rv_Tours_Cliente)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adaptador = ClienteToursAdapter(requireContext(), tourList) { tour ->
            Toast.makeText(requireContext(), "Tour: ${tour.nombre}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adaptador
        cargarToursCliente()
        return view
    }

    private fun cargarToursCliente(){
        db = FirebaseFirestore.getInstance()

        db.collection("tours")
            .get()
            .addOnSuccessListener { documento ->
                tourList.clear()
                for (document in documento){
                    val tour = document.toObject(Tour::class.java).copy(id = document.id)
                    tourList.add(tour)
                }
                adaptador.notifyDataSetChanged()
            }
            .addOnFailureListener{ e ->
                Toast.makeText(requireContext(), "Error al cargar tours", Toast.LENGTH_SHORT).show()
            }
    }
}