package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R

class NuevosToursAdapter(
    private val lista : List<Tour>
) : RecyclerView.Adapter<NuevosToursAdapter.NuevoTourViewHolder>() {
    inner class NuevoTourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imagen : ImageView = itemView.findViewById(R.id.iv_ImagenNuevoTour)
        val nombreTour : TextView = itemView.findViewById(R.id.tv_NombreNuevoTourCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NuevoTourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nuevotour, parent, false)
        return NuevoTourViewHolder(view)
    }

    override fun onBindViewHolder(holder: NuevoTourViewHolder, position: Int) {
        val tour = lista[position]
        holder.nombreTour.text = tour.nombre
        Glide.with(holder.itemView.context)
            .load(tour.imagenUrl)
            .into(holder.imagen)
    }

    override fun getItemCount(): Int = lista.size
}