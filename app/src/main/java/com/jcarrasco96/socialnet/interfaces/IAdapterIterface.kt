package com.jcarrasco96.socialnet.interfaces

import android.view.View
import com.jcarrasco96.socialnet.models.Post

interface IAdapterIterface {

    fun buttonPressedCall(post: Post)

    fun buttonPressedDelete(post: Post)

    fun buttonPressedMail(post: Post)

    fun buttonPressedReport(post: Post, view: View)

    fun buttonPressedReply(post: Post)

    fun onCLick(post: Post)

}
