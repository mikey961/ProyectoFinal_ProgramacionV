package com.universidad.prograv.proyecto_programacionv.Fragmentos.Registro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.Activities.Generales.AdminDashboardActivity
import com.universidad.prograv.proyecto_programacionv.R
import com.universidad.prograv.proyecto_programacionv.Activities.Generales.homeActivity

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var emailInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var btnLogin : MaterialButton
    private lateinit var btnVolver : MaterialButton
    private lateinit var auth : FirebaseAuth
    private lateinit var emailLayout : TextInputLayout
    private lateinit var passwordLayout : TextInputLayout
    private lateinit var emailError : TextView
    private lateinit var passwordError : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        emailInput = view.findViewById(R.id.emailInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        btnLogin = view.findViewById(R.id.loginButton)
        btnVolver = view.findViewById(R.id.volverButton)
        emailLayout = view.findViewById(R.id.emailLayout)
        passwordLayout = view.findViewById(R.id.passwordLayout)
        emailError = view.findViewById(R.id.emailError)
        passwordError = view.findViewById(R.id.passwordError)
        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener{
            limpiarErrores()

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty()) {
                mostrarErrorEmail("Por favor ingrese su correo electronico")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                mostrarErrorPassword("Por favor ingrese su contraseña")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid
                        Log.d("FIREBASE_DEBUG", "UID actual: $uid")

                        if (uid == null){
                            Toast.makeText(requireContext(), "UID de usuario no valido!", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        val db = FirebaseFirestore.getInstance()

                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()){
                                    val userRole = document.getString("role")?.trim() ?: ""
                                    val isSuperAdmin = document.getBoolean("superAdmin") ?: false
                                    Log.d("FIREBASE_DEBUG", "Rol en Firestore: $userRole")

                                    when(userRole.lowercase()){
                                        "administrador" -> {
                                            val intent = Intent(requireContext(), AdminDashboardActivity::class.java)
                                            startActivity(intent)
                                            requireActivity().finishAffinity()

                                            if (isSuperAdmin){
                                                Toast.makeText(requireContext(), "Bienvenido administrador principal", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(requireContext(), "Bienvenido administrador", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        "cliente" -> {
                                            val intent = Intent(requireContext(), homeActivity::class.java)
                                            startActivity(intent)
                                            requireActivity().finishAffinity()
                                            Toast.makeText(requireContext(), "Bienvenido Cliente", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            Toast.makeText(requireContext(), "Rol no autorizado o incorrecto", Toast.LENGTH_SHORT).show()
                                            auth.signOut()
                                        }
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Usuario no registrado correctamente", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener{ e ->
                                Toast.makeText(requireContext(), "Error al verificar permisos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        val error = task.exception
                        when (error) {
                            is FirebaseAuthInvalidUserException -> {
                                mostrarErrorEmail("El correo electronico no existe")
                            }

                            is FirebaseAuthInvalidCredentialsException -> {
                                mostrarErrorPassword("La contraseña es incorrecta")
                            }

                            is FirebaseAuthException -> {
                                //Error general
                                when (error.errorCode) {
                                    "ERROR_INVALID_EMAIL" -> mostrarErrorEmail("El formato del correo no es valido")
                                    else -> mostrarErrorPassword("Error desconocido: ${error.localizedMessage}")
                                }
                            }
                            else -> {
                                mostrarErrorPassword("Ups! ${error?.localizedMessage}")
                            }
                        }
                    }
                }
        }

        val forgotPassword = view.findViewById<TextView>(R.id.forgotPassword)
        forgotPassword.setOnClickListener{
            showPasswordResetDialog()
        }

        btnVolver.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return view
    }

    private fun showPasswordResetDialog(){
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Recuperar Contraseña")

        val input = EditText(requireContext())
        input.hint = "Ingrese su correo electronico"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Enviar"){ _, _ ->
            val email = input.text.toString().trim()
            if(email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(requireContext(), "Ingrese un correo valido!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun sendPasswordResetEmail(email : String){
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    Toast.makeText(requireContext(), "Se ha enviado un enlace de recuperacion a $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun mostrarErrorEmail(mensaje : String) {
        emailLayout.boxStrokeColor = Color.RED
        emailError.text = mensaje
        emailError.visibility = View.VISIBLE
    }

    private fun mostrarErrorPassword(mensaje : String) {
        passwordLayout.boxStrokeColor = Color.RED
        passwordError.text = mensaje
        passwordError.visibility = View.VISIBLE
    }

    private fun limpiarErrores() {
        emailLayout.boxStrokeColor = Color.GRAY
        passwordLayout.boxStrokeColor = Color.GRAY
        emailError.visibility = View.GONE
        passwordError.visibility = View.GONE
    }
}