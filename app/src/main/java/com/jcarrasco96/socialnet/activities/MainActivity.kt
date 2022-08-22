package com.jcarrasco96.socialnet.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.jcarrasco96.socialnet.R
import com.jcarrasco96.socialnet.databinding.ActivityMainBinding
import com.jcarrasco96.socialnet.databinding.DialogAboutBinding
import com.jcarrasco96.socialnet.interfaces.IFragmentInterface
import com.jcarrasco96.socialnet.ui.home.HomeFragment
import com.jcarrasco96.socialnet.ui.profile.ProfileFragment
import com.jcarrasco96.socialnet.utils.Preferences
import com.jcarrasco96.socialnet.utils.Utils


class MainActivity : AppCompatActivity(), IFragmentInterface {

    private lateinit var binding: ActivityMainBinding

    private var fragment: Fragment? = null
    private lateinit var fragmentTransaction: FragmentTransaction
    private var viewIsAtHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        if (!Preferences.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadData()

        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    showFragment(HomeFragment(this), "Inicio", true)
                    true
                }
                R.id.navigation_profile -> {
                    showFragment(ProfileFragment(this), "Perfil", false)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadData() {
        binding.navView.selectedItemId = R.id.navigation_home
        showFragment(HomeFragment(this), "Inicio", true)
    }

    override fun showFragment(iFragment: Fragment, title: String, isHome: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.popBackStack()

        if (fragment != null) {
            transaction.remove(fragment!!)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            transaction.commit()
        }

        viewIsAtHome = isHome
        fragment = iFragment

        supportActionBar?.setDisplayHomeAsUpEnabled(!isHome)

        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_body, fragment!!).commit()
        supportActionBar?.title = title
    }

    override fun onBackPressed() {
        if (!viewIsAtHome) {
            loadData()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.mnu_profile -> {
                startActivity(Intent(this, EditProfileActivity::class.java))
//                startActivity(Intent(this, TestActivity::class.java))
                true
            }
            R.id.mnu_about -> {
                val dialogBinding = DialogAboutBinding.inflate(layoutInflater)
                val dialog = Utils.showDialog(this, dialogBinding.root, true)

                dialogBinding.tvVersion.text =
                    "Version: " + Utils.versionApp(this.packageManager, this.packageName)
                dialogBinding.btClose.setOnClickListener {
                    dialog.dismiss()
                }
                dialogBinding.btPortfolio.setOnClickListener {
                    startActivity(
                        Intent(
                            "android.intent.action.VIEW", Uri.parse("https://cmsagency.com.es")
                        )
                    )
                }
                dialog.show()
                true
            }
            R.id.mnu_logout -> {
                Preferences.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}