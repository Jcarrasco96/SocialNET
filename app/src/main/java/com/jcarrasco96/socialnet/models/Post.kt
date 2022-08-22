package com.jcarrasco96.socialnet.models

import java.text.SimpleDateFormat
import java.util.*

class Post(
    val id: Int,
    val content: String,
    private val created_at: String,
    val status_post: String,
    val user_id: Int,
    val name: String,
    val email: String,
    private val is_verified: Int,
    val phone: String?,
    val avatar: String?
) {

    fun date(): Date? {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        return simpleDateFormat.parse(created_at)
    }

    fun isVerified(): Boolean {
        return is_verified == 1
    }

}