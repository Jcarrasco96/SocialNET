package com.jcarrasco96.socialnet.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.jcarrasco96.socialnet.BuildConfig
import com.jcarrasco96.socialnet.R
import com.squareup.picasso.Picasso

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = true): View =
    LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)

fun ImageView.loadUrl(url: String) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_user_placeholder)
        .into(this)
}

fun ImageView.loadImageProfile(avatar: String?) {
    Picasso.get()
        .load(BuildConfig.BASE_URL_MEDIA + "media/profile/" + avatar)
        .placeholder(R.drawable.ic_user_placeholder)
        .into(this)
}