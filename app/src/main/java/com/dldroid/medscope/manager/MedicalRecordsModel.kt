package com.dldroid.medscope.manager

class MedicalRecordsModel(private val name: String, private val url: String) {

    fun getName(): String {
        return name
    }

    fun getUrl(): String {
        return url
    }

}