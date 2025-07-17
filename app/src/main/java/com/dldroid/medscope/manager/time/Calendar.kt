package com.dldroid.medscope.manager.time

import android.app.DatePickerDialog
import android.content.Context
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class Calendar(private val context: Context?, private val dateBirth: TextInputEditText?) {

    fun showCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val mounth = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val dialog = DatePickerDialog(
            context!!, { _, day, mounth, year ->
                var mounth = mounth
                mounth += 1
                val date = "$day-$mounth-$year"
                dateBirth!!.setText(date)
            }, year, mounth, day
        )
        dialog.show()
    }

    fun showCalendar(context: Context?, text: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val mounth = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val dialog = DatePickerDialog(
            context!!, { _, day, mounth, year ->
                var mounth = mounth
                mounth += 1
                val date = "$day-$mounth-$year"
                text.text = date
            }, year, mounth, day
        )
        dialog.show()
    }
}