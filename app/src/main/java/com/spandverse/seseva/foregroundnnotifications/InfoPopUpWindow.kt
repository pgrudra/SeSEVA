package com.spandverse.seseva.foregroundnnotifications

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.spandverse.seseva.R

class InfoPopUpWindow {
    //PopupWindow display method
    fun showPopupWindow(view: View) {

        //Create a View object yourself through inflater
        val inflater = view.context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.info_pop_up_window, null)

        //Specify the length and width through constants
        val mul=view.context.resources.displayMetrics.scaledDensity
        //val width= 400*mul
        val width=(230*mul).toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        //Make Inactive Items Outside Of PopupWindow
        val focusable = true
        //Create a window with our parameters
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        //Set the location of the window on the screen
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
            //popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,62*mul, viewLocation[1]+(0.8*view.height).toInt())
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,viewLocation[0]-width+(56*mul).toInt(), viewLocation[1]+(0.8*view.height).toInt())

        //Initialize the elements of our window, install the handler
        val text: TextView = popupView.findViewById(R.id.categories)
        text.setText(
            when (view.id) {
                R.id.i_social -> R.string.categoriy_social
                R.id.i_communication -> R.string.category_communication
                R.id.i_entertainment -> R.string.categoriy_entertainment
                R.id.i_games -> R.string.category_games
                R.id.i_msnbs -> R.string.category_msnbs
                R.id.i_others -> R.string.category_others
                R.id.i_video -> R.string.category_video
                R.id.i->R.string.meaningOfThisWeeK
                else -> R.string.no_text
            }
        )

    }
}