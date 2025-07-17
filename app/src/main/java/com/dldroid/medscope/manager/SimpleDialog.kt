package com.dldroid.medscope.manager

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.dldroid.medscope.R
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.ui.LoginActivity

class SimpleDialog {
    companion object{
        fun showDialog(context: Context, title: String, message: String){
            val activity = context as Activity
            val builder = AlertDialog.Builder(
                context, R.style.dialog
            )
            val dialog = builder.setTitle(title)
                .setIcon(R.drawable.ic_logout)
                .setMessage(message)
                .setPositiveButton(
                    context.resources.getString(R.string.ok)
                ) { _, _ ->
                    val sh: SharedPreferences =
                        context.getSharedPreferences(AUTH, MODE_PRIVATE)
                    val e: SharedPreferences.Editor = sh.edit()
                    e.clear()

                    e.apply()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    activity.finish()
                }
                .create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(context.resources.getColor(R.color.blue))
            }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

}