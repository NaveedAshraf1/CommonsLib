package com.lymors.lycommons

import android.content.Context
import android.widget.Toast

class Utils {
    fun showLyCommonsToast(context: Context , message:String){
          Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}