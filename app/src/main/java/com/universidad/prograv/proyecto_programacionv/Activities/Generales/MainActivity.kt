package com.universidad.prograv.proyecto_programacionv.Activities.Generales

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.universidad.prograv.proyecto_programacionv.R

class MainActivity : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.loginButon)
        btnLogin.setOnClickListener {
            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        auth.signOut()
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        Log.d("FIREBASE_ROLE", "Rol leído en MainActivity: $role")
                        when (role) {
                            "Administrador" -> {
                                startActivity(Intent(this, AdminDashboardActivity::class.java))
                                finish()
                            }
                            "Cliente" -> {
                                startActivity(Intent(this, homeActivity::class.java))
                                finish()
                            }
                            else -> {
                                auth.signOut()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                                mostrarBotonLogin()
                            }
                        }
                    } else {
                        auth.signOut()
                        mostrarBotonLogin()
                    }
                }
                .addOnFailureListener {
                    Log.e("FIREBASE", "Error: ${it.message}")
                    mostrarBotonLogin()
                }
        } else {
            mostrarBotonLogin()
        }
    }

    private fun mostrarBotonLogin() {
        btnLogin.visibility = View.VISIBLE
    }
}