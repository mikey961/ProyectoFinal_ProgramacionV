package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R

class FavoritosAdapter(
    private var lista : MutableList<Tour>,
    private val onVerTour : (Tour) -> Unit,
    private val onQuitarFavorito : (Tour) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder>(){
    inner class FavoritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val ivImagen : ImageView = itemView.findViewById(R.id.iv_ImagenFavoritos)
        val tvNombre : TextView = itemView.findViewById(R.id.tv_NombreFavoritos)
        val ivFavToggle : ImageView = itemView.findViewById(R.id.iv_FavToggle)
        val tvFecha : TextView = itemView.findViewById(R.id.tv_FechaFavoritos)
        val tvHorarios : TextView = itemView.findViewById(R.id.tv_HorariosFavoritos)
        val btnVerTour : MaterialButton = itemView.findViewById(R.id.buttonVerTourFavoritos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favoritos, parent, false)
        return FavoritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        val tour = lista[position]

        holder.tvNombre.text = tour.nombre ?: "Sin nombre"
        holder.tvFecha.text = tour.fecha ?: "-"
        holder.tvHorarios.text = if (!tour.horarios.isNullOrEmpty()){
            tour.horarios.joinToString(", ")
        } else {
            "-"
        }

        Glide.with(holder.itemView.context)
            .load(tour.imagenUrl)
            .into(holder.ivImagen)

        holder.btnVerTour.setOnClickListener { onVerTour(tour) }
        holder.ivFavToggle.setImageResource(R.drawable.ic_favoritos)
        ImageViewCompat.setImageTintList(
            holder.ivFavToggle,
            ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.Rojo))
        )
        holder.ivFavToggle.setOnClickListener { onQuitarFavorito(tour) }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista : MutableList<Tour>){
        lista = nuevaLista
        notifyDataSetChanged()
    }
}