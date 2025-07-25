package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.universidad.prograv.proyecto_programacionv.R

class TourAdapter(
    private val context : Context,
    private val listaTours : List<Map<String, Any>>,
    private val onVerMas : (Map<String, Any>) -> Unit,
    private val onEditar : (Map<String, Any>) -> Unit,
    private val onEliminar : (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<TourAdapter.TourViewHolder>(){
    inner class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenTour : ImageView = itemView.findViewById(R.id.iv_ImagenTour)
        val nombreTour : TextView = itemView.findViewById(R.id.tv_NombreTour)
        val fechaTour : TextView = itemView.findViewById(R.id.tv_FechaTour)
        val horarioTour : TextView = itemView.findViewById(R.id.tv_HorasTour)
        val btnVerMas : MaterialButton = itemView.findViewById(R.id.buttonVerMas)
        val btnMenu : ImageButton = itemView.findViewById(R.id.buttonMenuOpciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tour, parent, false)
        return  TourViewHolder(view)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val tour = listaTours[position]

        holder.nombreTour.text = tour["nombre"] as? String ?: ""
        holder.fechaTour.text = tour["fecha"] as? String ?: ""

        val horarios = tour["horarios"] as? List<*> ?: emptyList<String>()
        holder.horarioTour.text = horarios.joinToString(", ")

        val imagenUrl = tour["imagenUrl"] as? String
        if (!imagenUrl.isNullOrEmpty()) {
            Glide.with(context).load(imagenUrl).into(holder.imagenTour)
        }

        holder.btnVerMas.setOnClickListener {
            onVerMas(tour)
        }

        holder.btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(context, view)
            MenuInflater(context).inflate(R.menu.menu_opciones_tour, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.opcion_editar -> onEditar(tour)
                    R.id.opcion_eliminar -> onEliminar(tour)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = listaTours.size
}