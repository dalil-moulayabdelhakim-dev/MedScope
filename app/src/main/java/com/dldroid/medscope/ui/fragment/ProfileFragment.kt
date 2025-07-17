package com.dldroid.medscope.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.DATE_OF_BIRTH
import com.dldroid.medscope.database.EMAIL
import com.dldroid.medscope.database.ERROR
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.GENDER
import com.dldroid.medscope.database.ID_CARD_NUMBER
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.PHONE
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.database.USER_PROFILE_URL
import com.dldroid.medscope.database.USER_TYPE
import com.dldroid.medscope.databinding.FragmentProfileBinding
import com.dldroid.medscope.manager.AuthManager
import com.dldroid.medscope.ui.LoginActivity
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = requireActivity().getSharedPreferences(AUTH, Context.MODE_PRIVATE)
        Log.i(
            "ProfileFragment.onViewCreated",
            sharedPreferences.getString(USER_TYPE, "").toString()
        )
        if (!sharedPreferences.getString(USER_TYPE, "").equals("2")) {
            binding.specialtyField.visibility = View.GONE
        }
        loadProfile(sharedPreferences)
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setListeners() {
        binding.logoutButton.setOnClickListener {
            context?.let {
                val builder = AlertDialog.Builder(context, R.style.dialog)
                val dialog = builder.setTitle("Logout")
                    .setIcon(R.drawable.ic_logout)
                    .setMessage(getString(R.string.do_you_want_to_logout))
                    .setPositiveButton(
                        getString(R.string.yes)
                    ) { _, _ ->
                        //Manager(this@HomeScreen).dontBack()
                        AuthManager.logout(
                            requireContext(),
                            requireActivity().supportFragmentManager
                        )
                    }
                    .setNegativeButton(getString(R.string.no),
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.cancel() })
                    .create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.blue))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.blue))
                }
                dialog.show()
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            loadProfile(requireActivity().getSharedPreferences(AUTH, Context.MODE_PRIVATE))
        }
    }

    private fun loadProfile(sharedPreferences: SharedPreferences) {
        val request: StringRequest = object : StringRequest(
            Method.POST, USER_PROFILE_URL,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val success = jsonObject.getBoolean(SUCCESS)
                if (success) {
                    binding.userName.text = jsonObject.getString(FULL_NAME)
                    binding.userRole.text = jsonObject.getString(USER_TYPE)
                    binding.userId.text = jsonObject.getString(ID_CARD_NUMBER)
                    binding.userBirth.text = jsonObject.getString(DATE_OF_BIRTH)
                    if (jsonObject.getInt(GENDER) == 1) {
                        binding.userGender.text = getString(R.string.male)
                    } else {
                        binding.userGender.text = getString(R.string.female)
                    }
                    binding.userEmail.text = jsonObject.getString(EMAIL)
                    binding.userPhone.text = jsonObject.getString(PHONE)
                    AuthManager.generateCode(
                        jsonObject.getString(ID_CARD_NUMBER),
                        binding.userQrCode
                    )
                    if (sharedPreferences.getString(USER_TYPE, "").equals("2")) {
                        binding.userSpecialtyDoctor.text = jsonObject.getString(USER_TYPE)
                    }

                } else {
                    val title = jsonObject.getString(TITLE)
                    val message = jsonObject.getString(MESSAGE)
                    val builder = AlertDialog.Builder(
                        context, R.style.dialog
                    )
                    val dialog = builder.setTitle(title)
                        .setIcon(R.drawable.ic_logout)
                        .setMessage(message)
                        .setPositiveButton(
                            getString(R.string.ok)
                        ) { _, _ ->
                            AuthManager.logout(requireContext(), requireActivity().supportFragmentManager)
                        }
                        .create()
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(resources.getColor(R.color.blue))
                    }
                    dialog.show()
                    AuthManager.logout(
                        requireContext(),
                        requireActivity().supportFragmentManager
                    )
                }
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                }
            }, Response.ErrorListener { error ->
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
                        getString(R.string.ok)
                    ) { _, _ ->
                        val sh: SharedPreferences =
                            requireContext().getSharedPreferences(AUTH, 0x0000)
                        val e: SharedPreferences.Editor = sh.edit()
                        e.clear()

                        //Manager(getApplicationContext()).dontBack()
                        e.apply()
                        requireContext().startActivity(Intent(context, LoginActivity::class.java))
                        requireActivity().finish()
                    }
                    .create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.blue))
                }
                dialog.show()
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                }

                Log.e("ProfileFragment.loadProfile.error", error.toString())
            }) {
            override fun getParams(): MutableMap<String, String> {
                val param: MutableMap<String, String> = HashMap()
                param[ACCESS_TOKEN] = sharedPreferences.getString(ACCESS_TOKEN, "").toString()
                return param
            }
        }

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)

    }
}