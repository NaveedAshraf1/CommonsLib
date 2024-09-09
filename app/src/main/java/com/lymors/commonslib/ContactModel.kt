package com.lymors.commonslib

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactModel (
    var key:String = "",
    var name:String = "",
    var phone:String = "",
    var status:String = "",
    var imageUrl:String = "",
): Parcelable