package com.universidad.prograv.proyecto_programacionv.Fragmentos.Registro

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.R
import com.universidad.prograv.proyecto_programacionv.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private var db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener{
            val nombre = binding.nombreInput.text.toString().trim()
            val apellido = binding.apellidoInput.text.toString().trim()
            val role = "Administrador"
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            if (!validarCampos(nombre, apellido, email, password, confirmPassword)){
                return@setOnClickListener
            }

            binding.registerButton.isEnabled = false
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener{ checkTask ->
                    if (checkTask.isSuccessful){
                        if (checkTask.result?.signInMethods?.isNotEmpty() == true){
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener { authResult ->
                                    authResult.user?.delete()?.addOnCompleteListener{ deleteTask ->
                                        if (deleteTask.isSuccessful){
                                            crearUsuarioYGuardarDatos(nombre, apellido, role, email, password)
                                        } else {
                                            binding.registerButton.isEnabled = true
                                            Toast.makeText(requireContext(), "Error al eliminar usuario existente: ${deleteTask.exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                .addOnFailureListener{ singInError ->
                                    binding.registerButton.isEnabled = true
                                    Toast.makeText(requireContext(), "Error al autenticar usuario existente: ${singInError.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            crearUsuarioYGuardarDatos(nombre, apellido, role, email, password)
                        }
                    } else {
                        binding.registerButton.isEnabled = true
                        Toast.makeText(requireContext(), "Error verificando email: ${checkTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun crearUsuarioYGuardarDatos(nombre: String, apellido: String, role: String, email: String, password : String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ createTask ->
                if (createTask.isSuccessful){
                    guardarDataUsuario(nombre, apellido, role, email)
                    Toast.makeText(requireContext(), "¡Registro exitoso!", Toast.LENGTH_LONG).show()
                    limpiarCampos()
                } else {
                    binding.registerButton.isEnabled = true
                    Toast.makeText(requireContext(), "Error al registrar: ${createTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun guardarDataUsuario(nombre : String, apellido : String, role : String, email : String){
        val user = auth.currentUser
        user?.let {
            val userData = hashMapOf(
                "nombre" to nombre,
                "apellido" to apellido,
                "role" to role,
                "email" to email,
                "superAdmin" to false,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(user.uid)
                .set(userData)
                .addOnFailureListener{ e ->
                    Toast.makeText(requireContext(), "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        binding.registerButton.isEnabled = true
    }

    private fun validarCampos(nombre : String, apellido : String, email : String, password : String, confirmPassword : String) : Boolean{
        var esValido = true

        //Validar campo nombre
        if (TextUtils.isEmpty(nombre)){
            binding.nameError.text = "El nombre es requerido"
            binding.nameError.visibility = View.VISIBLE
            esValido = false
        } else {
            binding.nameError.visibility = View.GONE
        }

        //Validar campo apellido
        if (TextUtils.isEmpty(apellido)){
            binding.apellidoError.text = "El apellido es requerido"
            binding.apellidoError.visibility = View.VISIBLE
            esValido = false
        } else {
            binding.apellidoError.visibility = View.GONE
        }

        //Validar campo email
        if (TextUtils.isEmpty(email)){
            binding.emailError.text = "El correo electrónico es requerido"
            binding.emailError.visibility = View.VISIBLE
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailError.text = "Correo electrónico no valido"
            binding.emailError.visibility = View.VISIBLE
            esValido = false
        } else {
            binding.emailError.visibility = View.GONE
        }

        //Validar campo Contraseña
        if (TextUtils.isEmpty(password)){
            binding.passwordError.text = "La contraseña es requerida"
            binding.passwordError.visibility = View.VISIBLE
            esValido = false
        } else if (password.length < 8) {
            binding.passwordError.text = "La contraseña debe tener al menos 8 caracteres"
            binding.passwordError.visibility = View.VISIBLE
            esValido = false
        } else {
            binding.passwordError.visibility = View.GONE
        }

        //Validar campo confirmar contraseña
        if (TextUtils.isEmpty(confirmPassword)){
            binding.confirmPasswordError.text = "Es necesario confirmar la contraseña"
            binding.confirmPasswordError.visibility = View.VISIBLE
            esValido = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordError.text = "Las contraseñas no coinciden"
            binding.confirmPasswordError.visibility = View.VISIBLE
            esValido = false
        } else {
            binding.confirmPasswordError.visibility = View.GONE
        }
        return esValido
    }

    private fun limpiarCampos(){
        binding.nombreInput.text?.clear()
        binding.apellidoInput.text?.clear()
        binding.emailInput.text?.clear()
        binding.passwordInput.text?.clear()
        binding.confirmPasswordInput.text?.clear()

        binding.nameError.visibility = View.GONE
        binding.apellidoError.visibility = View.GONE
        binding.emailError.visibility = View.GONE
        binding.passwordError.visibility = View.GONE
        binding.confirmPasswordError.visibility = View.GONE
    }
}