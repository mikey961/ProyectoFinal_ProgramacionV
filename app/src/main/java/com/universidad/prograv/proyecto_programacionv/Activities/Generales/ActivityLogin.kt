package com.universidad.prograv.proyecto_programacionv.Activities.Generales

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Registro.LoginFragment
import com.universidad.prograv.proyecto_programacionv.Fragmentos.Registro.RegisterFragment
import com.universidad.prograv.proyecto_programacionv.R


class ActivityLogin : AppCompatActivity() {
    private lateinit var btnLogIn : MaterialButton
    private lateinit var btnsingUp : MaterialButton
    private lateinit var tabSelector : View
    private var isLoginSelected = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogIn = findViewById(R.id.tabLogin)
        btnsingUp = findViewById(R.id.tabSingUp)
        tabSelector = findViewById(R.id.tabSelector)

        mostrarFragmento(LoginFragment())
        actualizarEstiloBotones()
        tabSelector.post{
            moverTabSelector(true)
        }

        btnLogIn.setOnClickListener {
            if (!isLoginSelected) {
                isLoginSelected = true
                mostrarFragmento(LoginFragment())
                actualizarEstiloBotones()
            }
        }

        btnsingUp.setOnClickListener {
            if (isLoginSelected) {
                isLoginSelected = false
                mostrarFragmento(RegisterFragment())
                actualizarEstiloBotones()
            }
        }
    }

    private fun mostrarFragmento(fragmento : Fragment){
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, fragmento)
            .commit()
    }

    private fun actualizarEstiloBotones(){
        if (isLoginSelected) {
            btnLogIn.setBackgroundResource(R.drawable.btn_selected_red)
            btnsingUp.setBackgroundResource(R.drawable.btn_unselected_green)
        } else {
            btnLogIn.setBackgroundResource(R.drawable.btn_unselected_green)
            btnsingUp.setBackgroundResource(R.drawable.btn_selected_red)
        }
    }

    private fun moverTabSelector(toLogin : Boolean){
        val parentWidth = btnLogIn.width + btnsingUp.width
        val targetX = if (toLogin) 0f else (parentWidth / 2).toFloat()

        tabSelector.animate()
            .translationX(targetX)
            .setDuration(300)
            .start()
    }
}