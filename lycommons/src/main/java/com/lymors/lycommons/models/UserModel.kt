package com.lymors.lycommons.models


data class UserModel(
    override var key: String="",
    var name:String = "",
    var token:String="",
): ParentModel(key)