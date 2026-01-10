package com.lymors.commonslib

import android.app.Application
import com.lymors.lycommons.utils.SharedPreferencesHelper.Companion.sharedPref
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application(){
    override fun onCreate() {
        super.onCreate()

//        MyLibs.initilize(applicationContext)
    }

}