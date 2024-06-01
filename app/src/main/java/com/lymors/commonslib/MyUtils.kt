package com.lymors.commonslib

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.lymors.lycommons.utils.DialogUtil

object MyUtils {

    private var dialogUtil: DialogUtil? = null
    val Fragment.dialogUtil: DialogUtil
        get() = getInstance()

    val Activity.dialogUtil: DialogUtil
        get() = getInstance()



    private fun getInstance(): DialogUtil {
        if (dialogUtil == null) {
            dialogUtil = DialogUtil()
        }
        return dialogUtil!!
    }
}