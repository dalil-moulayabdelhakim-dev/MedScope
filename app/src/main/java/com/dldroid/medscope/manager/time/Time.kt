package com.dldroid.medscope.manager.time

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class Time(context: Context?, dateBirth: TextInputEditText?) {
    private var hour = 0
    private var min: Int = 0

    fun showTimePicker(context: Context?, text: TextView) {
        val tp = OnTimeSetListener { timePicker, i, i1 ->
            hour = i
            min = i1
            text.text = String.format(Locale.getDefault(), "%02d:%02d", hour, min)
        }
        val tpd = TimePickerDialog(context, tp, hour, min, true)
        tpd.show()
    }
}