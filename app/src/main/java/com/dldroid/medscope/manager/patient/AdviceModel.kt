package com.dldroid.medscope.manager.patient

class AdviceModel(private var advice: String?, private var image: Int) {


    fun getText(): String? {
        return advice
    }

    fun getImage(): Int {
        return image
    }
}