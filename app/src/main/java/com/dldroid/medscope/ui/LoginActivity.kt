package com.dldroid.medscope.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.EMAIL
import com.dldroid.medscope.database.ERROR
import com.dldroid.medscope.database.LOGIN_URL
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.PASSWORD
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.USER_TYPE
import com.dldroid.medscope.databinding.ActivityLoginBinding
import com.dldroid.medscope.ui.dialog.SimpleProgressDialog
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue)
        initViews()
        setListeners()
    }

    private fun initViews() {
        try {
            val version = packageManager
                .getPackageInfo(packageName, 0)
                .versionName
            binding.appVersion.text = "v$version"
        } catch (e: PackageManager.NameNotFoundException) {
            binding.appVersion.text = "vN/A"
        }
        binding.submitButton.setEnabled(false)
        binding.submitButton.setBackgroundResource(R.drawable.shape_submit_button_enabeled)
    }

    private fun login() {
        val progressDialog = SimpleProgressDialog()

        val email: String = binding.emailLogInput.text.toString().trim()
        val pass: String = binding.passwordLogInput.text.toString().trim()
        binding.emailLogLayout.error = ""
        binding.passLogLayout.error = ""
        progressDialog.show(supportFragmentManager, "progress_dialog")
        val request: StringRequest = object : StringRequest(
            Method.POST, LOGIN_URL,
            Response.Listener<String?> { response ->
                progressDialog.dismiss()
                try {
                    val j = JSONObject(response)
                    val s = j.getBoolean(SUCCESS)

                    if (s) {
                        val accessToken = j.getString(ACCESS_TOKEN)
                        val userType = j.getString(USER_TYPE)
                        val sharedP = getSharedPreferences(AUTH, MODE_PRIVATE).edit()
                        sharedP.putString(ACCESS_TOKEN, accessToken)
                        sharedP.putString(USER_TYPE, userType)
                        sharedP.apply()

                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            j.getString(ERROR),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    progressDialog.dismiss()
                    e.printStackTrace()
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.something_went_wrong_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LoginActivity.login.onResponse.JSONException", e.toString())
                }
            }, Response.ErrorListener { error ->
                progressDialog.dismiss()

                val jsonObject = JSONObject(error.networkResponse.data.toString(Charsets.UTF_8))
                val message = jsonObject.getString(MESSAGE)

                Toast.makeText(
                    this@LoginActivity,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("LoginActivity.login.onErrorResponse", error.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val p: MutableMap<String, String> = HashMap()
                p[EMAIL] = email
                p[PASSWORD] = pass
                return p
            }
        }

        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(request)
    }

    private val loginWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val password: String = binding.passwordLogInput.text.toString().trim()
            val email: String = binding.emailLogInput.text.toString().trim()



            if (password.isNotEmpty() && email.isNotEmpty()) {
                binding.submitButton.setEnabled(true)
                binding.submitButton.setBackgroundResource(R.drawable.shape_submit_button)
            } else {
                binding.submitButton.setEnabled(false)
                binding.submitButton.setBackgroundResource(R.drawable.shape_submit_button_enabeled)
            }
        }

        override fun afterTextChanged(s: Editable) {
        }
    }

    private fun setListeners() {
        binding.emailLogInput.addTextChangedListener(loginWatcher)
        binding.passwordLogInput.addTextChangedListener(loginWatcher)

        binding.submitButton.setOnClickListener {
            login()
        }

        binding.passwordLogInput.setOnEditorActionListener { _, actionId, _ ->
            if ( actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                true
            }else{
                false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}