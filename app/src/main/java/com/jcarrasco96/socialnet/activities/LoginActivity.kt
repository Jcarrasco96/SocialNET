package com.jcarrasco96.socialnet.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jcarrasco96.socialnet.databinding.ActivityLoginBinding
import com.jcarrasco96.socialnet.models.ApiError
import com.jcarrasco96.socialnet.models.LoginResult
import com.jcarrasco96.socialnet.services.API
import com.jcarrasco96.socialnet.utils.Preferences
import com.jcarrasco96.socialnet.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginSigin.btSignUp.setOnClickListener {
            Utils.flip(binding.loginRegister.layoutSignup, binding.loginSigin.layoutLogin)
        }
        binding.loginRegister.btLogin.setOnClickListener {
            Utils.flip(binding.loginSigin.layoutLogin, binding.loginRegister.layoutSignup)
        }

        binding.loginSigin.btnSigIn.setOnClickListener {
            val username = binding.loginSigin.txtUsername.text.toString().trim()
            val password = binding.loginSigin.txtPassword.text.toString().trim()

            when {
                username.isEmpty() -> Utils.showSnack(this, "Usuario no puede estar vacío")
                password.isEmpty() -> Utils.showSnack(this, "Contraseña no puede estar vacío")
                else -> login(username, password)
            }
        }
        binding.loginRegister.btnSignUp.setOnClickListener {
            val username: String = binding.loginRegister.txtUsernameS.text.toString().trim()
            val password: String = binding.loginRegister.txtPasswordS.text.toString().trim()
            val password2: String = binding.loginRegister.txtPassword2S.text.toString().trim()
            val email: String = binding.loginRegister.txtEmailS.text.toString().trim()

            when {
                username.isEmpty() -> Utils.showSnack(this, "Usuario no puede estar vacío")
                email.isEmpty() -> Utils.showSnack(this, "Correo electrónico no puede estar vacío")
                password.isEmpty() -> Utils.showSnack(this, "Contraseña no puede estar vacío")
                password2.isEmpty() -> Utils.showSnack(
                    this,
                    "Repetir contraseña no puede estar vacío"
                )
                password != password2 -> Utils.showSnack(this, "Las contraseñas no coinciden")
                else -> register(username, email, password, password2)
            }
        }
    }

    private fun login(username: String, password: String) {
        enableLogin(false)

        API.authLogin(username, password, object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                if (response.isSuccessful) {
                    val loginResult = response.body()
                    if (loginResult != null) {
                        Preferences.login(loginResult)
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Utils.showSnack(this@LoginActivity, "Response empty!")
                    }
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(this@LoginActivity, apiError.message)
                    }
                }
                enableLogin(true)
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                Log.e("ERROR", "API.authService().login", t)
                Utils.showSnack(this@LoginActivity, t.message.toString())
                enableLogin(true)
            }
        })
    }

    private fun register(username: String, email: String, password: String, password2: String) {
        enableRegister(false)

        API.authRegister(username, password, password2, email, object : Callback<ApiError> {
            override fun onResponse(call: Call<ApiError>, response: Response<ApiError>) {
                if (response.isSuccessful) {
                    Utils.flip(
                        binding.loginSigin.layoutLogin, binding.loginRegister.layoutSignup
                    )
                    binding.loginSigin.txtUsername.setText(username)
                    binding.loginSigin.txtPassword.setText(password)
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(this@LoginActivity, apiError.message)
                    }
                }
                enableRegister(true)
            }

            override fun onFailure(call: Call<ApiError>, t: Throwable) {
                Log.e("ERROR", "API.authService().register", t)
                Utils.showSnack(this@LoginActivity, t.message.toString())
                enableRegister(true)
            }
        })
    }

    fun enableLogin(enable: Boolean) {
        binding.loginSigin.let {
            it.txtUsername.isEnabled = enable
            it.txtPassword.isEnabled = enable

            it.btSignUp.isEnabled = enable

            if (enable) {
                it.progressLogin.isVisible = false
                it.btnSigIn.isVisible = true
            } else {
                it.progressLogin.isVisible = true
                it.btnSigIn.isVisible = false
            }
        }
    }

    fun enableRegister(enable: Boolean) {
        binding.loginRegister.let {
            it.txtUsernameS.isEnabled = enable
            it.txtPasswordS.isEnabled = enable
            it.txtPassword2S.isEnabled = enable
            it.txtEmailS.isEnabled = enable

            it.btLogin.isEnabled = enable

            if (enable) {
                it.progressSignup.isVisible = false
                it.btnSignUp.isVisible = true
            } else {
                it.progressSignup.isVisible = true
                it.btnSignUp.isVisible = false
            }
        }
    }

}