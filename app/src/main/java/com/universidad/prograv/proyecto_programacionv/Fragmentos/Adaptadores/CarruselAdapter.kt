package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.universidad.prograv.proyecto_programacionv.Activities.Cliente.VerTourActivity
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R

class CarruselAdapter(
    private val lista : List<Tour>
) : RecyclerView.Adapter<CarruselAdapter.CarruselViewHolder>() {
    inner class CarruselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imagen : ImageView = itemView.findViewById(R.id.iv_ImagenCarrusel)
        val tutuloTour : TextView = itemView.findViewById(R.id.tv_NombreTourCarruselCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarruselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrusel, parent, false)
        return CarruselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarruselViewHolder, position: Int) {
        val tour = lista[position]
        holder.tutuloTour.text = tour.nombre
        Glide.with(holder.itemView.context)
            .load(tour.imagenUrl)
            .into(holder.imagen)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VerTourActivity::class.java)
            intent.putExtra("tour", tour)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = lista.size
}