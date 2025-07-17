package com.dldroid.medscope.manager

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.ALL_USER_TYPES
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.GET_ALL_USER_TYPES_URL
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.LOGOUT_URL
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.NAME
import com.dldroid.medscope.database.PASSWORD_LENGTH
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.database.USER_TYPE
import com.dldroid.medscope.ui.LoginActivity
import com.dldroid.medscope.ui.dialog.SimpleProgressDialog
import com.dldroid.medscope.ui.fragment.home.admin.ViewUserDetailsActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONException
import org.json.JSONObject
import java.util.Random

class AuthManager {

    companion object {

        fun logout(context: Context, supportFragmentManager: FragmentManager) {
            val activity = context as Activity
            val simpleProgressDialog = SimpleProgressDialog()
            simpleProgressDialog.show(supportFragmentManager, "SimpleProgressDialog")
            val r: StringRequest = object : StringRequest(
                Method.POST, LOGOUT_URL,
                Response.Listener<String?> { response ->
                    simpleProgressDialog.dismiss()
                    try {
                        val j = JSONObject(response)
                        val b = j.getBoolean(SUCCESS)
                        if (b) {
                            val sh: SharedPreferences =
                                context.getSharedPreferences(AUTH, 0x0000)
                            val e: SharedPreferences.Editor = sh.edit()
                            e.clear()

                            //Manager(getApplicationContext()).dontBack()
                            e.apply()
                            context.startActivity(Intent(context, LoginActivity::class.java))
                            activity.finish()
                        } else {
                            Toast.makeText(
                                context.applicationContext,
                                context.getString(R.string.something_went_wrong_please_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.e("MY_ERROR_AT_HOMESCR_2_IN_ONRESPONSE", e.toString())
                    }
                }, Response.ErrorListener { error ->
                    simpleProgressDialog.dismiss()
                    Log.e("MY_ERROR_AT_HOMESCR_2_IN_ONERRORRESPONSE", error.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val p: MutableMap<String, String> = HashMap()

                    val sh: SharedPreferences =
                        context.getSharedPreferences(AUTH, 0)
                    p[ACCESS_TOKEN] = sh.getString(ACCESS_TOKEN, "")!!
                    return p
                }
            }
            val rq = Volley.newRequestQueue(context.applicationContext)
            rq.add(r)
        }

        fun getUserType(context: Context): String {
            val sh = context.getSharedPreferences(AUTH, 0x0000)
            return sh.getString(USER_TYPE, "")!!
        }

        fun generateCode(code: String, view: ImageView) {
            val multiFormatWriter = MultiFormatWriter()
            try {
                val matrix = multiFormatWriter.encode(code, BarcodeFormat.QR_CODE, 600, 600)

                val encoder = BarcodeEncoder()

                val bitmap = encoder.createBitmap(matrix)
                //setBitmap
                view.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        fun loadUserTypes(context: Context, spinner: Spinner) {
            val userTypes = ArrayList<String>()
            userTypes.add("Select User Type")
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, userTypes)
            val userTypesId = ArrayList<String>()
            userTypesId.add("0")
            val request = object : StringRequest(
                Method.POST,
                GET_ALL_USER_TYPES_URL,
                Response.Listener { response ->
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getBoolean(SUCCESS)
                    if (success) {
                        val data = jsonObject.getJSONArray(ALL_USER_TYPES)
                        for (i in 0 until data.length()) {
                            userTypes.add(data.getJSONObject(i).getString(NAME))
                            userTypesId.add(data.getJSONObject(i).getString(ID))
                        }
                        Log.d("UserTypes", userTypes.toString())
                        adapter.setDropDownViewResource(R.layout.item_spinner)
                        spinner.adapter = adapter

                    }

                }, Response.ErrorListener { error ->

                    if (error?.networkResponse?.data != null) {
                        // Process the response data here
                        val jsonObject = JSONObject(String(error.networkResponse.data))
                        val title = jsonObject.getString(TITLE)
                        val message = jsonObject.getString(MESSAGE)
                        val builder = AlertDialog.Builder(
                            context, R.style.dialog
                        )
                        val dialog = builder.setTitle(title)
                            .setIcon(R.drawable.ic_logout)
                            .setMessage(message)
                            .setPositiveButton(
                                context.getString(R.string.ok)
                            ) { _, _ ->
                                val sh: SharedPreferences =
                                    context.getSharedPreferences(AUTH, 0x0000)
                                val e: SharedPreferences.Editor = sh.edit()
                                e.clear()

                                //Manager(getApplicationContext()).dontBack()
                                e.apply()
                                context.startActivity(Intent(context, LoginActivity::class.java))
                                (context as Activity).finish()
                            }
                            .create()
                        dialog.setOnShowListener {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(context.resources.getColor(R.color.blue))
                        }
                        dialog.show()
                    } else {
                        // Handle the error appropriately, such as displaying an error message
                        Log.e("AuthManager", "Error fetching user types: ${error?.message}")
                    }
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    val sharedPreferences =
                        context.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                    params[ACCESS_TOKEN] = sharedPreferences.getString(ACCESS_TOKEN, "").toString()
                    return params
                }
            }
            Volley.newRequestQueue(context).add(request)
        }

        fun generatePassword(): String {
            val length: Int = PASSWORD_LENGTH
            val chars = "ABCDEFGHIJKLMNOPQRSTEVWXYZ1234567890abcdefghijklmnopqrstevwxyz".toCharArray()
            val r = Random()
            val b = StringBuilder()
            for (i in 0 until length) {
                val c = chars[r.nextInt(chars.size)]
                b.append(c)
            }
            return b.toString()
        }

    }




}