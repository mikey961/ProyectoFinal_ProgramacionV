package com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.CarruselAdapter
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.NuevosToursAdapter
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R


class ClienteDashboardFragment : Fragment() {
    private lateinit var tv_Bienvenida : TextView
    private lateinit var rvCarrusel : RecyclerView
    private lateinit var rvNuevosTours : RecyclerView
    private var db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cliente_dashboard, container, false)

        tv_Bienvenida = view.findViewById(R.id.tv_BienvenidaCliente)
        rvCarrusel = view.findViewById(R.id.rv_CarrouselTours)
        rvNuevosTours = view.findViewById(R.id.rv_NuevosTours)

        val verTodosLosTours = view.findViewById<TextView>(R.id.tv_VerTodos)
        verTodosLosTours.setOnClickListener {
            val navView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationCliente)
            navView.selectedItemId = R.id.nav_Tours_Cliente
        }

        mostrarNombreUsuario()
        cargarTours()

        return view
    }

    private fun mostrarNombreUsuario(){
        val uid = auth.currentUser?.uid
        if (uid != null){
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()){
                        val nombre = documento.getString("nombre") ?: ""
                        tv_Bienvenida.text = "Bienvenido, $nombre"
                    } else {
                        Toast.makeText(requireContext(), "El usuario no existe", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error el nombre del usuario", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarTours(){
        db.collection("tours")
            .get()
            .addOnSuccessListener { result ->
                val listaCarrusel = mutableListOf<Tour>()
                val listaNuevosTours = mutableListOf<Tour>()

                for (document in result){
                    val timestamp = document.getTimestamp("creadoEn")
                    val fecha = timestamp?.toDate()
                    val tour = Tour(
                        id = document.id,
                        nombre = document.getString("nombre"),
                        descripcion = document.getString("descripcion"),
                        duracion = document.getString("duracion"),
                        cantidadVehiculos = document.getLong("cantidadVehiculos")?.toInt(),
                        precioDoble = document.getDouble("precioDoble"),
                        precioIndividual = document.getDouble("precioIndividual"),
                        horarios = document.get("horarios") as? List<String>,
                        fecha = document.getString("fecha"),
                        imagenUrl = document.getString("imagenUrl"),
                        tipoTour = document.getString("tipoTour"),
                        creadoEn = fecha
                    )
                    listaCarrusel.add(tour)

                    val ahora = java.util.Date()
                    val ochoDiasEnSegundos = 8 * 24 * 60 * 60 * 1000

                    if (fecha != null && ahora.time - fecha.time <= ochoDiasEnSegundos) {
                        listaNuevosTours.add(tour)
                    }
                }
                configurarCarrusel(listaCarrusel)
                configurarNuevosTours(listaNuevosTours)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar tours", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarCarrusel(lista : List<Tour>){
        val adaptador = CarruselAdapter(lista)
        rvCarrusel.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCarrusel.adapter = adaptador
    }

    private fun configurarNuevosTours(lista : List<Tour>){
        val adaptador = NuevosToursAdapter(lista)
        rvNuevosTours.layoutManager = GridLayoutManager(requireContext(), 2)
        rvNuevosTours.adapter = adaptador
    }
}