package com.jcarrasco96.socialnet.models

import com.jcarrasco96.socialnet.models.json.UserJson

class User(
    val id: Int,
    val username: String,
    name: String?,
    last_name: String?,
    email: String,
    var auth: Int,
    val is_active: Int,
    var is_verified: Int,
    ci: String,
    sex: String,
    birthdate: String,
    country: String,
    phone: String?,
    address: String,
    private val avatar: String?,
    val ti_wallet: String?,
    var posts: Int,
    var comments: Int,
) : UserJson(
    name, last_name, email, phone, country, address, birthdate, ci, sex
) {

    constructor() : this(0, "", "", "", "", 0, 0, 0, "", "", "", "", "", "", "", "", 0, 0)

    fun sex(): String {
        return if (sex == "M") "Masculino" else "Femenino"
    }

    fun name(): String {
        return if (name.isNullOrEmpty() && last_name.isNullOrEmpty()) {
            username
        } else {
            "$name $last_name".trim()
        }
    }

    fun avatar500(): String {
        if (avatar.isNullOrEmpty()) {
            return ""
        }
        return avatar.replace(".png", "_r500.png")
    }

    fun auth(): String {
        return when (auth) {
            0 -> "Posteador"
            1 -> "A"
            2 -> "B"
            3 -> "Administrador"
            else -> "Posteador"
        }
    }

}
