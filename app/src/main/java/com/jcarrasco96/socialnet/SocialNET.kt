package com.jcarrasco96.socialnet

import android.app.Application
import com.jcarrasco96.socialnet.utils.Preferences

class SocialNET : Application() {

    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
    }

}
