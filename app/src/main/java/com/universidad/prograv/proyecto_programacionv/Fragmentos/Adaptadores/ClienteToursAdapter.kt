package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.universidad.prograv.proyecto_programacionv.R

class ClienteToursAdapter(
    private val context : Context,
    private val tours : List<Tour>,
    private val onVerTourClick : (Tour) -> Unit
) : RecyclerView.Adapter<ClienteToursAdapter.ClienteTourViewHolder>(){
    inner class ClienteTourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imagen : ImageView = itemView.findViewById(R.id.iv_ImagenTour)
        val nombre : TextView = itemView.findViewById(R.id.tv_NombreTour)
        val fecha : TextView = itemView.findViewById(R.id.tv_FechaTour)
        val horas : TextView = itemView.findViewById(R.id.tv_HorasTour)
        val btnVerTour : MaterialButton = itemView.findViewById(R.id.buttonVerTour)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteTourViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tour_cliente, parent, false)
        return ClienteTourViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteTourViewHolder, position: Int) {
        val tour = tours[position]

        holder.nombre.text = tour.nombre
        holder.fecha.text = tour.fecha
        holder.horas.text = tour.horarios?.joinToString(" | ")

        Glide.with(context).load(tour.imagenUrl).into(holder.imagen)

        holder.btnVerTour.setOnClickListener {
            onVerTourClick(tour)
        }
    }

    override fun getItemCount(): Int = tours.size
}