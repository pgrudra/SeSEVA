package com.spandverse.seseva

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

class ModeInfoPopUpWindow {
    fun showPopupWindow(view: View) {

        //Create a View object yourself through inflater
        val inflater = view.context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.mode_info_pop_up_window, null)

        //Specify the length and width through constants

        val width:Int=400*view.context.resources.displayMetrics.density.toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        //Make Inactive Items Outside Of PopupWindow
        val focusable = true
        //Create a window with our parameters
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        //Set the location of the window on the screen
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        when (view.id) {
            R.id.medium_info -> {
                popupWindow.showAtLocation(
                    view, Gravity.NO_GRAVITY,268, viewLocation[1]+(1.1*view.height).toInt())
            }
            R.id.strict_info -> {
                popupWindow.showAtLocation(
                    view, Gravity.NO_GRAVITY,200, viewLocation[1]+(1.1*view.height).toInt())
            }
            else -> {
                popupWindow.showAtLocation(
                    view, Gravity.NO_GRAVITY,190, viewLocation[1]+(1.1*view.height).toInt())
            }
        }

        //Initialize the elements of our window, install the handler
        val text: TextView = popupView.findViewById(R.id.mode_info_text)
        text.setText(
            when (view.id) {
                R.id.light_info->R.string.mode_light_info
                R.id.medium_info->R.string.mode_medium_info
                R.id.strict_info->R.string.mode_strict_into
                else -> R.string.no_text
            }
        )

    }
}