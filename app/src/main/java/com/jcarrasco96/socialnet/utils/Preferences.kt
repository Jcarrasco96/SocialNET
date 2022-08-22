package com.jcarrasco96.socialnet.utils

import android.content.Context
import android.content.SharedPreferences
import com.jcarrasco96.socialnet.models.LoginResult
import com.jcarrasco96.socialnet.models.User

object Preferences {

    private const val NAME = "socialnet_pref_network"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

//    fun bytesSend(bytesSend: Long) {
//        val bytes = preferences.getLong("PREF_BYTES_SEND", 0)
//        preferences.edit {
//            it.putLong("PREF_BYTES_SEND_LAST", bytes)
//            it.putLong("PREF_BYTES_SEND", bytes + bytesSend)
//        }
//    }

    var bytesSend: Long
        get() {
            return preferences.getLong("PREF_BYTES_SEND", 0)
        }
        set(value) {
            val bytes = preferences.getLong("PREF_BYTES_SEND", 0)
            preferences.edit {
                it.putLong("PREF_BYTES_SEND_LAST", bytes)
                it.putLong("PREF_BYTES_SEND", bytes + value)
            }
        }

    var bytesReceived: Long
        get() {
            return preferences.getLong("PREF_BYTES_RECEIVED", 0)
        }
        set(value) {
            val bytes = preferences.getLong("PREF_BYTES_RECEIVED", 0)
            preferences.edit {
                it.putLong("PREF_BYTES_RECEIVED_LAST", bytes)
                it.putLong("PREF_BYTES_RECEIVED", bytes + value)
            }
        }

//    fun bytesReceived(bytesReceived: Long) {
//        val bytes = preferences.getLong("PREF_BYTES_RECEIVED", 0)
//        preferences.edit {
//            it.putLong("PREF_BYTES_RECEIVED_LAST", bytes)
//            it.putLong("PREF_BYTES_RECEIVED", bytes + bytesReceived)
//        }
//    }

    fun token(): String {
        return preferences.getString("PREF_TOKEN", "").toString()
    }

    fun login(login: LoginResult) {
        preferences.edit {
            it.putString("PREF_TOKEN", login.token)
            it.putInt("PREF_AUTH", login.auth)
            it.putInt("PREF_USER_ID", login.id)
        }
    }

    fun logout() {
        preferences.edit {
            it.putString("PREF_TOKEN", null)
            it.putInt("PREF_AUTH", 0)
            it.putInt("PREF_USER_ID", 0)
        }
    }

    fun isLoggedIn(): Boolean {
        return token().isNotEmpty()
    }

    fun userId(): Int {
        return preferences.getInt("PREF_USER_ID", 0)
    }

    fun debugMode(): Boolean {
        return preferences.getBoolean("PREF_DEBUG_MODE", false)
    }

    fun saveCurrent(user: User) {
        preferences.edit {
            it.putInt("PREF_ID", user.id)
            it.putString("PREF_USERNAME", user.username)
        }
    }

}