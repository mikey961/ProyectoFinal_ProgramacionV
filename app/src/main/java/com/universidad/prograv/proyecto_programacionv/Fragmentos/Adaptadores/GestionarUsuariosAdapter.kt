package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.Modelos.Usuarios
import com.universidad.prograv.proyecto_programacionv.R

class GestionarUsuariosAdapter(
    private val context : Context,
    private var listaCompleta : MutableList<Usuarios>
) : RecyclerView.Adapter<GestionarUsuariosAdapter.UsuariosViewHolder>(){
    private var  listaFiltrada :MutableList<Usuarios> = listaCompleta.toMutableList()

    inner class UsuariosViewHolder(itemView : View) :RecyclerView.ViewHolder(itemView){
        val nombreApellido : TextView = itemView.findViewById(R.id.tv_NombreApellido_Usuario)
        val correo : TextView = itemView.findViewById(R.id.tv_Correo_Usuario)
        val tipoRole : MaterialAutoCompleteTextView = itemView.findViewById(R.id.actv_AsignarRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuariosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuarios, parent, false)
        return UsuariosViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuariosViewHolder, position: Int) {
        val usuario = listaFiltrada[position]
        holder.nombreApellido.text = "${usuario.nombre} ${usuario.apellido}"
        holder.correo.text = usuario.correo

        val roles = listOf("Administrador", "Cliente")
        val adaptador = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, roles)
        holder.tipoRole.setAdapter(adaptador)
        holder.tipoRole.setText(usuario.role, false)

        holder.tipoRole.setOnItemClickListener{ _, _, i, _ ->
            val nuevoRol = roles[i]

            MaterialAlertDialogBuilder(context)
                .setTitle("Confirmar cambio rol")
                .setMessage("¿Estas seguro de cambiar el rol del usuario a ${nuevoRol}")
                .setPositiveButton("SI"){ _, _ ->
                    FirebaseFirestore.getInstance().collection("users")
                        .document(usuario.uid)
                        .update("role", nuevoRol)
                        .addOnSuccessListener {
                            usuario.role = nuevoRol
                            Toast.makeText(context, "Rol actualizado a $nuevoRol", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al actualizar rol", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun filtrar(texto : String){
        listaFiltrada = if (texto.isEmpty()){
            listaCompleta.toMutableList()
        } else {
            listaCompleta.filter {
                it.nombre.contains(texto, ignoreCase = true) || it.apellido.contains(texto, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}