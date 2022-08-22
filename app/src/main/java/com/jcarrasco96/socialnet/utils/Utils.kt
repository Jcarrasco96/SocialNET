package com.jcarrasco96.socialnet.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.jcarrasco96.socialnet.databinding.SnackDarkBinding
import kotlin.math.roundToInt

object Utils {

    fun bytesToHuman(bytes: Long): String {
        var bytesReal: Float = bytes.toFloat()
        var index = 0
        val bytesList = arrayOf(" B", " KB", " MB", " GB", " TB", " PB", " EB", " ZB", " YB")

        if (bytesReal >= 1024) {
            do {
                bytesReal /= 1024
                index++
            } while (bytesReal >= 1024)
        }

        return "${bytesReal.roundToInt()} ${bytesList[index]}"
    }

    fun versionApp(packageManager: PackageManager, packageName: String): String {
        var versionName = "Unkown"
        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connMgr.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connMgr.activeNetwork.run {
                    val activeNetwork = connMgr.getNetworkCapabilities(this)
                    activeNetwork.run {
                        if (this == null) {
                            return false
                        }
                        return when {
                            this.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            this.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            this.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    }
                }
            } else {
                connMgr.activeNetworkInfo?.run {
                    return when (this.type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }

        return false
    }

    fun showErrorToast(context: Context, error: String) {
        var errorMessage = "Error en la red"
        if (Preferences.debugMode()) {
            errorMessage = error
            Log.d("ERROR", error)
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun showSnack(activity: Activity, title: String) {
        val make = Snackbar.make(
            activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_SHORT
        )
        val inflate = SnackDarkBinding.inflate(activity.layoutInflater)
        inflate.snackTitle.text = title
        make.view.setBackgroundColor(0)
        val snackbarLayout = make.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(inflate.root, 0)
        make.show()
//        Snackbar.make(activity.findViewById(android.R.id.content), title, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnack(view: View, inflater: LayoutInflater, title: String) {
        val make = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
        val inflate = SnackDarkBinding.inflate(inflater)
        inflate.snackTitle.text = title
        make.view.setBackgroundColor(0)
        val snackbarLayout = make.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(inflate.root, 0)
        make.show()
//        Snackbar.make(view, title, Snackbar.LENGTH_SHORT).show()
    }

    fun showDialog(context: Context, contentView: View, isTransparent: Boolean = false): Dialog {
        val dialog = Dialog(context)
        val layoutParams = WindowManager.LayoutParams()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(contentView)
        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = -2
        layoutParams.height = -2
        dialog.window!!.attributes = layoutParams
        if (isTransparent) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        return dialog
    }

    fun flip(toShow: View, toHide: View) {
        val visToInvis = ObjectAnimator.ofFloat(toHide, "alpha", 1F, 0F)
        visToInvis.duration = 500
        visToInvis.interpolator = AccelerateInterpolator()

        val invisToVis = ObjectAnimator.ofFloat(toShow, "alpha", 0F, 1F)
        invisToVis.duration = 500
        invisToVis.interpolator = DecelerateInterpolator()

        visToInvis.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                toHide.isVisible = false
            }

            override fun onAnimationStart(animation: Animator?) {
                invisToVis.start()
                toShow.isVisible = true
            }
        })

        visToInvis.start()
    }

}