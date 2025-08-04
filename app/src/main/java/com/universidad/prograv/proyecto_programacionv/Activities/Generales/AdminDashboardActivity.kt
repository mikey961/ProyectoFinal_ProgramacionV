package com.universidad.prograv.proyecto_programacionv.Activities.Generales

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador.AdminDashboardFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador.AdminMiPerfilFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador.AdminToursFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Administrador.GestionarUsuarios
import com.universidad.prograv.proyecto_programacionv.R

class AdminDashboardActivity : AppCompatActivity(){
    private  lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        bottomNav = findViewById(R.id.botonNavegacion)
        cargarFragmento(AdminDashboardFragment())

        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid!!)
            .get()
            .addOnSuccessListener { document ->
                val isSuperAdmin = document.getBoolean("superAdmin") ?: false
                if (isSuperAdmin){
                    bottomNav.menu.findItem(R.id.nav_gestionarUsuarios).isVisible = true
                }
            }

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.nav_dashboard -> cargarFragmento(AdminDashboardFragment())
                R.id.nav_tours -> cargarFragmento(AdminToursFragment())
                R.id.nav_gestionarUsuarios -> cargarFragmento(GestionarUsuarios())
                R.id.nav_MiPerfil -> cargarFragmento(AdminMiPerfilFragment())
                else -> false
            }
        }
    }

    private fun cargarFragmento(fragmento : Fragment) : Boolean{
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_Fragmentos, fragmento)
            .commit()
        return true
    }
}