package com.jcarrasco96.socialnet.interfaces

import androidx.fragment.app.Fragment

interface IFragmentInterface {

    fun showFragment(iFragment: Fragment, title: String, isHome: Boolean)

}