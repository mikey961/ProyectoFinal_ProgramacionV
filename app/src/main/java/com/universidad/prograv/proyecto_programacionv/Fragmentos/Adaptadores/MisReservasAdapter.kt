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
    private val listaReservas: MutableList<Reserva>,
    private var toursById: Map<String, Tour>,
    private val onCancelarClick: (Reserva) -> Unit,
    private val onRenovarClick: (Reserva) -> Unit
) : RecyclerView.Adapter<MisReservasAdapter.ReservaViewHolder>() {

    inner class ReservaViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val vtNombreTour: TextView = v.findViewById(R.id.tv_NombreTourReserva)
        val vtNombreUsuario: TextView = v.findViewById(R.id.tv_NombreUsuarioReserva)
        val vtCorreoUsuario: TextView = v.findViewById(R.id.tv_CorreoUsuarioReserva)
        val vtFechaHoraTour: TextView = v.findViewById(R.id.tv_FechaHoraTourReserva)
        val tvSolicitud: TextView = v.findViewById(R.id.tv_SolicitudCliente)
        val btnAccion: com.google.android.material.button.MaterialButton =
            v.findViewById(R.id.buttonCancelarReserva)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        ReservaViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_reserva, p, false))

    override fun onBindViewHolder(h: ReservaViewHolder, pos: Int) {
        val r = listaReservas[pos]

        h.vtNombreTour.text = r.nombreTour
        h.vtNombreUsuario.text = "${r.nombre} ${r.apellido}".trim()
        h.vtCorreoUsuario.text = r.correo

        val t = r.idTour?.let { toursById[it] }
        h.vtFechaHoraTour.text = "${t?.fecha.orEmpty()} - ${r.horaTour.orEmpty()}"

        h.tvSolicitud.visibility = View.GONE

        val ctx = h.itemView.context
        if (r.estado.equals("cancelada", true)) {
            h.btnAccion.text = "Renovar reserva"
            h.btnAccion.isEnabled = true
            h.btnAccion.alpha = 1f
            h.btnAccion.setBackgroundColor(
                androidx.core.content.ContextCompat.getColor(ctx, android.R.color.holo_green_dark)
            )
            h.btnAccion.setOnClickListener { onRenovarClick(r) }
        } else {
            h.btnAccion.text = "Cancelar reserva"
            h.btnAccion.isEnabled = true
            h.btnAccion.alpha = 1f
            h.btnAccion.setBackgroundColor(
                androidx.core.content.ContextCompat.getColor(ctx, android.R.color.holo_red_dark)
            )
            h.btnAccion.setOnClickListener { onCancelarClick(r) }
        }
    }

    override fun getItemCount() = listaReservas.size

    fun updateTours(newMap: Map<String, Tour>) {
        toursById = newMap; notifyDataSetChanged()
    }
}
