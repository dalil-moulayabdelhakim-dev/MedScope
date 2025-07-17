package com.dldroid.medscope.manager.patient

class SpecialtyModel(private val name : String, private val image : Int) {

    fun getName(): String {
        return name
    }

    fun getImage(): Int {
        return image
    }
}