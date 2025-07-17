package com.dldroid.medscope.manager

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.REMOVE_USER_URL
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.ui.LoginActivity
import com.dldroid.medscope.ui.dialog.SimpleProgressDialog
import org.json.JSONObject

class ToolsManager() {

    companion object {
        fun removeUser(
            context: Context,
            supportFragmentManager: FragmentManager,
            swipeRefreshLayout: SwipeRefreshLayout,
            id: String
        ) {

            val simpleProgressDialog = SimpleProgressDialog()
            simpleProgressDialog.show(supportFragmentManager, "loading")
            val request = object : StringRequest(
                Method.POST, REMOVE_USER_URL,
                Response.Listener { response ->
                    simpleProgressDialog.dismiss()
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getBoolean(SUCCESS)
                    if (success) {
                        Toast.makeText(context, jsonObject.getString(MESSAGE), Toast.LENGTH_SHORT).show()
                        (context as Activity).onBackPressed()
                    }

                }, Response.ErrorListener { error ->
                    simpleProgressDialog.dismiss()
                    val networkResponse = error.networkResponse
                    val statusCode = networkResponse.statusCode
                    val jsonObject = JSONObject(String(networkResponse.data))
                    val title = jsonObject.getString(TITLE)
                    val message = jsonObject.getString(MESSAGE)
                    val builder = AlertDialog.Builder(
                        context, R.style.dialog
                    )
                    lateinit var dialog: AlertDialog
                    builder.setTitle("$statusCode $title")
                        .setIcon(R.drawable.ic_logout)
                        .setMessage(message)
                    if (statusCode == 401) {
                        dialog = builder.setPositiveButton(
                            context.resources.getString(R.string.ok)
                        ) { _, _ ->
                            val sh: SharedPreferences =
                                context.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                            val e: SharedPreferences.Editor = sh.edit()
                            e.clear()

                            e.apply()
                            context.startActivity(
                                Intent(
                                    context,
                                    LoginActivity::class.java
                                )
                            )
                            (context as Activity).finish()
                        }.create()
                    } else {
                        dialog = builder.setPositiveButton(
                            context.resources.getString(R.string.ok)
                        ) { _, _ ->
                            dialog.dismiss()
                        }.create()
                    }

                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(context.resources.getColor(R.color.blue))
                    }
                    dialog.show()
                    swipeRefreshLayout.isRefreshing = false
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params[ACCESS_TOKEN] = context.getSharedPreferences(AUTH, Context.MODE_PRIVATE).getString(ACCESS_TOKEN, "").toString()
                    params["id"] = id
                    return params
                }
            }
            Volley.newRequestQueue(context).add(request)
        }
    }

}