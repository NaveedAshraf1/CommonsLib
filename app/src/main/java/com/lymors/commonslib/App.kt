package com.lymors.commonslib

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.lymors.lycommons.utils.MyLibs
import com.lymors.lycommons.utils.Utils
import com.lymors.lycommons.utils.Utils.sharedPref
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application(){
    override fun onCreate() {
        super.onCreate()
        sharedPref
//        MyLibs.initilize(applicationContext)
    }

}