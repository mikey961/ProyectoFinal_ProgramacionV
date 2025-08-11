package com.universidad.prograv.proyecto_programacionv.Fragmentos.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.universidad.prograv.proyecto_programacionv.Modelos.Reserva
import com.universidad.prograv.proyecto_programacionv.R

class ReservasAdminAdapter(
    private val lista: MutableList<Reserva>,
    private val onCancelarAdmin: (Reserva) -> Unit,
    private val onRenovarAdmin: (Reserva) -> Unit
) : RecyclerView.Adapter<ReservasAdminAdapter.VH>() {

    inner class VH(v: View): RecyclerView.ViewHolder(v) {
        val tvNombreTour: TextView = v.findViewById(R.id.tv_NombreTour_Admin)
        val tvNombreUsuario: TextView = v.findViewById(R.id.tv_Cliente_Admin)
        val tvCorreo: TextView = v.findViewById(R.id.tv_Correo_Admin)
        val tvFechaHora: TextView = v.findViewById(R.id.tv_FechaHora_Admin)
        val tvSolicitud: TextView = v.findViewById(R.id.tv_Solicitud_Roja)
        val btnAccion: com.google.android.material.button.MaterialButton =
            v.findViewById(R.id.button_Cancelar_Admin)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_reserva_admin, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val r = lista[pos]
        val ctx = h.itemView.context
        val nombreCompleto = "${r.nombre} ${r.apellido}".trim()

        h.tvNombreTour.text = r.nombreTour
        h.tvNombreUsuario.text = nombreCompleto
        h.tvCorreo.text = r.correo
        h.tvFechaHora.text = "${r.fechaTour.orEmpty()} - ${r.horaTour.orEmpty()}"

        when {
            r.cancelRequested == true -> {
                h.tvSolicitud.visibility = View.VISIBLE
                h.tvSolicitud.setTextColor(
                    androidx.core.content.ContextCompat.getColor(ctx, android.R.color.holo_red_dark)
                )
                h.tvSolicitud.text = "El cliente $nombreCompleto solicitó cancelar la reserva"
            }
            r.renewRequested == true -> {
                h.tvSolicitud.visibility = View.VISIBLE
                h.tvSolicitud.setTextColor(
                    androidx.core.content.ContextCompat.getColor(ctx, android.R.color.holo_green_dark)
                )
                h.tvSolicitud.text = "El cliente $nombreCompleto solicitó renovar la reserva"
            }
            else -> h.tvSolicitud.visibility = View.GONE
        }

        val isCancelada = r.estado.equals("cancelada", true)
        val puedeCancelar = r.cancelRequested == true && !isCancelada
        val puedeRenovar = r.renewRequested == true || isCancelada

        if (puedeRenovar) {
            h.btnAccion.text = "Renovar reserva"
            h.btnAccion.isEnabled = r.renewRequested == true
            h.btnAccion.alpha = if (r.renewRequested == true) 1f else 0.5f
            h.btnAccion.backgroundTintList =
                androidx.core.content.ContextCompat.getColorStateList(ctx, android.R.color.holo_green_dark)
            h.btnAccion.setOnClickListener {
                if (r.renewRequested == true) onRenovarAdmin(r)
                else android.widget.Toast.makeText(ctx, "El cliente no ha solicitado renovar esta reserva.", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            h.btnAccion.text = "Cancelar reserva"
            h.btnAccion.isEnabled = puedeCancelar
            h.btnAccion.alpha = if (puedeCancelar) 1f else 0.5f
            h.btnAccion.backgroundTintList =
                androidx.core.content.ContextCompat.getColorStateList(ctx, android.R.color.holo_red_dark)
            h.btnAccion.setOnClickListener {
                if (puedeCancelar) onCancelarAdmin(r)
                else android.widget.Toast.makeText(ctx, "El cliente no ha solicitado cancelar esta reserva.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = lista.size
}