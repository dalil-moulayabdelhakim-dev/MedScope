package com.dldroid.medscope.ui.fragment.home.admin.section

import android.os.Bundle
import android.view.View
import com.dldroid.medscope.ui.fragment.home.admin.AdminSectionHelperFragment

class DoctorSection : AdminSectionHelperFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userType = "2"
    }
}