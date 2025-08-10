package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.universidad.prograv.proyecto_programacionv.Modelos.Reserva
import com.universidad.prograv.proyecto_programacionv.Modelos.Tour
import com.universidad.prograv.proyecto_programacionv.R

class MisReservasAdapter(
    private val listaReservas : MutableList<Reserva>,
    toursMap : Map<String, Tour>,
    private val onCancelarClick : (Reserva) -> Unit
) : RecyclerView.Adapter<MisReservasAdapter.ReservaViewHolder>(){

    var toursById: Map<String, Tour> = toursMap
        private set

    inner class ReservaViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val vtNombreTour : TextView = itemView.findViewById(R.id.tv_NombreTourReserva)
        val vtNombreUsuario : TextView = itemView.findViewById(R.id.tv_NombreUsuarioReserva)
        val vtCorreoUsuario : TextView = itemView.findViewById(R.id.tv_CorreoUsuarioReserva)
        val vtFechaHoraTour : TextView = itemView.findViewById(R.id.tv_FechaHoraTourReserva)
        val btnCancelar : TextView = itemView.findViewById(R.id.buttonCancelarReserva)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        val reserva = listaReservas[position]

        holder.vtNombreTour.text = reserva.nombreTour
        holder.vtNombreUsuario.text = "${reserva.nombre} ${reserva.apellido}"
        holder.vtCorreoUsuario.text = reserva.correo

        val tour = reserva.idTour?.let { toursById[it] }

        val fecha = reserva.fechaTour?.takeIf { it.isNotBlank() } ?: tour?.fecha.orEmpty()
        val hora = reserva.horaTour?.takeIf { it.isNotBlank() } ?: tour?.horarios?.firstOrNull().orEmpty()
        holder.vtFechaHoraTour.text = "$fecha - $hora"

        holder.btnCancelar.setOnClickListener {
            onCancelarClick(reserva)
        }
    }

    override fun getItemCount(): Int = listaReservas.size

    fun updateTours(newMap: Map<String, Tour>) {
        toursById = newMap
        notifyDataSetChanged()
    }
}