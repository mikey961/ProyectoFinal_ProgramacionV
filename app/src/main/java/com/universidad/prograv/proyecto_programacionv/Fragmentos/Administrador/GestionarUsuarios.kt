package com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores.GestionarUsuariosAdapter
import com.universidad.prograv.proyecto_programacionv.Modelos.Usuarios
import com.universidad.prograv.proyecto_programacionv.R

class GestionarUsuarios : Fragment() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var adaptador : GestionarUsuariosAdapter
    private lateinit var buscador : TextInputEditText
    private val usuarios = mutableListOf<Usuarios>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gestionar_usuarios, container, false)

        recyclerView = view.findViewById(R.id.rv_GestionUsuarios)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adaptador = GestionarUsuariosAdapter(requireContext(), usuarios)
        recyclerView.adapter = adaptador
        buscador = view.findViewById(R.id.tiet_BuscarUsuario)

        cargarUsuarios()

        buscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adaptador.filtrar(s.toString())
            }
        })

        return view
    }

    private fun cargarUsuarios(){
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                usuarios.clear()
                for (document in result){
                    val usuario = Usuarios(
                        uid = document.id,
                        nombre = document.getString("nombre") ?: "",
                        apellido = document.getString("apellido") ?: "",
                        correo = document.getString("email") ?: "",
                        role = document.getString("role") ?: "Cliente",
                    )
                    usuarios.add(usuario)
                }
                adaptador.filtrar("")
            }
    }

}