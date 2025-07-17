package com.dldroid.medscope.manager.appointment

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.CREATE_APPOINTMENT_URL
import com.dldroid.medscope.database.ERROR
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.ACCEPT_APPOINTMENT_URL
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.CANCEL_APPOINTMENT_URL
import com.dldroid.medscope.database.DATE
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.LOCATION
import com.dldroid.medscope.database.PATIENT_NAME
import com.dldroid.medscope.database.RECEPTOR_ID
import com.dldroid.medscope.database.REASON
import com.dldroid.medscope.database.RECEPTOR_NAME
import com.dldroid.medscope.database.STATUS
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TIME
import com.dldroid.medscope.ui.dialog.SimpleProgressDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppointmentManager(private val context: Context?,
                         private val receptorID: String?,
                         private val date: String?,
                         private val time: String?,
                         private val reason: String?,
                         private val location: String?) {

    private val queue : RequestQueue = Volley.newRequestQueue(context)

    fun createAppointment(supportFragmentManager: FragmentManager) {
       // Log.d("app_details", "$PATIENT_ID: $patientID, $RECEPTOR_ID: $receptorID, $DATE: $date, $TIME: $time, $LOCATION: $location, $REASON: $reason")
        val simpleProgressDialog = SimpleProgressDialog()
        simpleProgressDialog.show(supportFragmentManager, "CreateAppointment")
        val r: StringRequest = object : StringRequest(
            Method.POST, CREATE_APPOINTMENT_URL,
            Response.Listener { response ->
                simpleProgressDialog.dismiss()
                try {
                    val j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        if (j.getString(ACCESS_TOKEN).isNotEmpty()){
                            val database = Firebase.database
                            val myRef = context?.resources?.getString(R.string.appointments)
                                ?.let { database.getReference(it).child(j.getString(ACCESS_TOKEN)).child("message") }
                            //myRef.setValue("Med Scope:\\nMr/Ms ${j.getString(RECEPTOR_NAME)} you have a new appointment request from patient ${j.getString(PATIENT_NAME)}, please enter the app to check it.")
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            val currentTime = sdf.format(Date()) // Example: "12/03/2025 14:30"

                            myRef!!.setValue("You have a new Appointment request at $currentTime")
                            Toast.makeText(context, "The appointment created", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context, "This Receptor not available, please try again later", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Log.e("MY_ERROR_AT_CREATEAPP_IN_ELSE", j.getString(ERROR))
                        Toast.makeText(
                            context,
                            "somthing wont wrong, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("MY_ERROR_AT_CREATEAPP_IN_ONRESPONSE", e.message!!)
                }
            },
            Response.ErrorListener { error ->

                simpleProgressDialog.dismiss()
                try {
                    Log.e("error_content", error.toString())
                   /* val jsonObject = JSONObject(error.networkResponse.data.toString(Charsets.UTF_8))
                    //val title = jsonObject.getString(TITLE)
                    val message = jsonObject.getString(ERRORS)
                    if (context != null) {
                        SimpleDialog.showDialog(context, "title", message)
                    }*/
                }catch (e: JSONException){
                    e.printStackTrace()
                }

            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val p: MutableMap<String, String> = HashMap()
                val sh = context!!.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                val accessToken = sh.getString(ACCESS_TOKEN, "")
                p[ACCESS_TOKEN] = accessToken.toString()
                p[RECEPTOR_ID] = receptorID!!
                p[DATE] = date.toString()
                p[TIME] = time.toString()
                p[LOCATION] = location!!
                p[REASON] = reason!!
                return p
            }
        }

        queue.add(r)
    }

    fun updateAppointment(appointment: AppointmentManager?) {
    }

    fun acceptAppointment(id: String) {
        val r: StringRequest = object : StringRequest(
            Method.POST, ACCEPT_APPOINTMENT_URL,
            Response.Listener<String?> { response ->
                try {
                    val j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "somthing wont wrong, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()

                    Log.e("MY_ERROR_AT_aceptAPP_IN_ONRESPONSE", e.message!!)
                }
            },
            Response.ErrorListener { error ->
                Log.e(
                    "MY_ERROR_AT_CREATEAPP_IN_ONERRORRESPONSE",
                    error.toString()
                )
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val p: MutableMap<String, String> = HashMap()
                p[ID] = id
                p[STATUS] = 1.toString()
                return p
            }
        }

        queue.add(r)
    }

    fun cancelAppointment(id: String, msg: String) {
        val r: StringRequest = object : StringRequest(
            Method.POST, CANCEL_APPOINTMENT_URL,
            Response.Listener<String?> { response ->
                try {
                    val j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "somthing wont wrong, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()

                    Log.e("MY_ERROR_AT_CANCELAPP_IN_ONRESPONSE", e.toString())
                }
            },
            Response.ErrorListener { error ->
                Log.e(
                    "MY_ERROR_AT_CANCELAPP_IN_ONRERRORRESPONSE",
                    error.toString()
                )
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val p: MutableMap<String, String> = HashMap()
                p[ID] = id
                p[MESSAGE] = msg
                return p
            }
        }

        queue.add(r)
    }
}