package com.universidad.prograv.proyecto_programacionv.Activities.Generales

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente.ClienteCarritoFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente.ClienteDashboardFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente.ClienteMiPerfilFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente.ClienteMisReservasFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Cliente.ClienteToursFragment
import com.universidad.prograv.proyecto_programacionv.R

class homeActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNav = findViewById(R.id.bottomNavigationCliente)

        cargarFragmento(ClienteDashboardFragment())

        bottomNav.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.nav_Dashboard_Cliente -> cargarFragmento(ClienteDashboardFragment())
                R.id.nav_Tours_Cliente -> cargarFragmento(ClienteToursFragment())
                R.id.nav_MisReservas_Cliente -> cargarFragmento(ClienteMisReservasFragment())
                R.id.nav_Carrito_Cliente -> cargarFragmento(ClienteCarritoFragment())
                R.id.nav_MiPerfil_Cliente -> cargarFragmento(ClienteMiPerfilFragment())
                else -> false
            }
        }
    }

    private fun cargarFragmento(fragmento : Fragment) : Boolean{
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerCliente, fragmento)
            .commit()
        return true
    }
}