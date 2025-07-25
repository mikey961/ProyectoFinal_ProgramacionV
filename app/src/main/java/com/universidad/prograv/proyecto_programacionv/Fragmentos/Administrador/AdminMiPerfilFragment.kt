package com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.Activities.Generales.ActivityLogin
import com.universidad.prograv.proyecto_programacionv.R

class AdminMiPerfilFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var tvNombre_Apellido : TextView
    private lateinit var tvCorreo : TextView
    private lateinit var btnCambiarPassword : MaterialButton
    private lateinit var btnCerrarSesion : MaterialButton


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_mi_perfil, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        tvNombre_Apellido = view.findViewById(R.id.tvNombreApellido)
        tvCorreo = view.findViewById(R.id.tvCorreo)
        btnCambiarPassword = view.findViewById(R.id.buttonCambiarContraseña)
        btnCerrarSesion = view.findViewById(R.id.buttonCerrarSesion)

        val uid = auth.currentUser?.uid
        if (uid != null){
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()){
                        val nombre = documento.getString("nombre") ?: ""
                        val apellido = documento.getString("apellido") ?: ""
                        val correo = documento.getString("email") ?: ""

                        tvNombre_Apellido.text = "Bienvenido, $nombre $apellido"
                        tvCorreo.text = correo
                    } else {
                        Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        btnCambiarPassword.setOnClickListener {
            mostrarBottomSheetCambiarPassword()
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), ActivityLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return  view
    }

    private fun mostrarBottomSheetCambiarPassword(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_cambiar_password, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(bottomSheetView)

        val passActual = bottomSheetView.findViewById<EditText>(R.id.tiet_ContraseñaActual)
        val newPass = bottomSheetView.findViewById<EditText>(R.id.tiet_NuevaContraseña)
        val confirmNewPass = bottomSheetView.findViewById<EditText>(R.id.tiet_RepetirNuevaContraseña)
        val errorActual = bottomSheetView.findViewById<TextView>(R.id.actualPasswordError)
        val erroNuevo = bottomSheetView.findViewById<TextView>(R.id.newPasswordError)
        val errorConfirmar = bottomSheetView.findViewById<TextView>(R.id.confirmPasswordError   )
        val btnCambiarPassword = bottomSheetView.findViewById<MaterialButton>(R.id.buttonCambiarContraseña)

        fun limpiarErrores(){
            errorActual?.visibility = View.GONE
            erroNuevo?.visibility = View.GONE
            errorConfirmar?.visibility = View.GONE
        }

        btnCambiarPassword?.setOnClickListener {
            limpiarErrores()

            val actual = passActual.text.toString()
            val nueva = newPass.text.toString()
            val confirmar = confirmNewPass.text.toString()

            var esValido = true

            if (actual.isEmpty()){
                errorActual?.text = "La contraseña actual es requerida"
                errorActual?.visibility = View.VISIBLE
                esValido = false
            }

            if (actual.isEmpty()){
                erroNuevo?.text = "La contraseña nueva es requerida"
                erroNuevo?.visibility = View.VISIBLE
                esValido = false
            } else if (nueva.length < 8){
                erroNuevo?.text = "La contraseña debe tener al menos 8 caracteres"
                erroNuevo?.visibility = View.VISIBLE
                esValido = false
            }

            if (confirmar.isEmpty()){
                errorConfirmar?.text = "Es necesario confirmar la nueva contraseña"
                errorConfirmar?.visibility = View.VISIBLE
                esValido = false
            } else if (nueva != confirmar){
                errorConfirmar?.text = "Las contraseñas no coinciden"
                errorConfirmar?.visibility = View.VISIBLE
                esValido = false
            }

            if (!esValido) return@setOnClickListener

            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email

            if (user != null || email != null) {
                val credenciales = com.google.firebase.auth.EmailAuthProvider.getCredential(email!!, actual)

                user.reauthenticate(credenciales)
                    .addOnSuccessListener{
                        user.updatePassword(nueva)
                            .addOnSuccessListener{
                                db.collection("users")
                                    .document(user.uid)
                                    .update("password", nueva)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "El cambio de contraseña fue exitoso!", Toast.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    }
                                    .addOnFailureListener{ e ->
                                        Toast.makeText(requireContext(), "Error al actualizar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener{ e->
                                Toast.makeText(requireContext(), "Erro al cambiar contraseña: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener{
                        Toast.makeText(requireContext(), "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Sesion no valida!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}