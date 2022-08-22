package com.jcarrasco96.socialnet.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.jcarrasco96.socialnet.R
import com.jcarrasco96.socialnet.databinding.ActivityEditProfileBinding
import com.jcarrasco96.socialnet.databinding.DialogVerifyEmailBinding
import com.jcarrasco96.socialnet.models.ApiError
import com.jcarrasco96.socialnet.models.AvatarResult
import com.jcarrasco96.socialnet.models.User
import com.jcarrasco96.socialnet.models.json.UserJson
import com.jcarrasco96.socialnet.services.API
import com.jcarrasco96.socialnet.utils.*
import com.jcarrasco96.socialnet.utils.Constants.REQUEST_READ_EXTERNAL_STORAGE
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditProfileBinding
    private lateinit var user: User
    private lateinit var componentActivity: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableFields(false)

        user = User()

        loadProfile()

        binding.cUserUpdate.spinnerCountryCode.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, CountryCode.Country
        )
        binding.cUserUpdate.spinnerCountryCode.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    user.country = CountryCode.Country[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.cUserUpdate.spinnerSex.adapter = ArrayAdapter.createFromResource(
            this, R.array.sex, android.R.layout.simple_spinner_dropdown_item
        )
        binding.cUserUpdate.spinnerSex.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    user.sex = if (position == 0) "M" else "F"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.fabChangeAvatar.setOnClickListener {
            changeAvatar()
        }

        binding.cUserUpdate.btnUpdateProfile.setOnClickListener {
            val name = binding.cUserUpdate.itemFirstName.text.toString()
            val lastName = binding.cUserUpdate.itemLastName.text.toString()
            val email = binding.cUserUpdate.itemEmail.text.toString()
            val phone = binding.cUserUpdate.itemPhone.text.toString()
//            val country = binding.cUserUpdate.itemCountry.text.toString()
            val address = binding.cUserUpdate.itemAddress.text.toString()
            val birthdate = binding.cUserUpdate.itemBirthdate.text.toString()
            val identification = binding.cUserUpdate.itemIdentification.text.toString()

            when {
                email.isEmpty() -> {
                    Utils.showSnack(this, "Correo electrónico no puede estar vacío")
                    binding.cUserUpdate.itemEmail.requestFocus()
                }
                birthdate.isEmpty() -> {
                    Utils.showSnack(this, "Fecha de nacimiento no puede estar vacío")
                    binding.cUserUpdate.itemEmail.requestFocus()
                }
                !Time.checkDate(birthdate) -> {
                    Utils.showSnack(this, "Fecha de nacimiento no tiene un formato correcto")
                    binding.cUserUpdate.itemBirthdate.requestFocus()
                }
                else -> {
                    update(
                        name, lastName, email, phone, user.country, address, birthdate, identification
                    )
                }
            }
        }

        binding.cUserWallet.btnChangeWallet.setOnClickListener {
            val wallet = binding.cUserWallet.itemWallet.text.toString()
            changeWallet(wallet)
        }

        binding.cUserChangePassword.btnChangePassword.setOnClickListener {
            val oldPassword = binding.cUserChangePassword.itemOldPassword.text.toString()
            val password = binding.cUserChangePassword.itemPassword.text.toString()
            val password2 = binding.cUserChangePassword.itemPassword2.text.toString()

            when {
                oldPassword.isEmpty() -> {
                    Utils.showSnack(this, "Contraseña actual no puede estar vacío")
                    binding.cUserChangePassword.itemOldPassword.requestFocus()
                }
                password.isEmpty() -> {
                    Utils.showSnack(this, "Nueva contraseña no puede estar vacío")
                    binding.cUserChangePassword.itemPassword.requestFocus()
                }
                password2.isEmpty() -> {
                    Utils.showSnack(this, "Nueva contraseña no puede estar vacío")
                    binding.cUserChangePassword.itemPassword2.requestFocus()
                }
                password != password2 -> {
                    Utils.showSnack(this, "Contraseñas no coinciden")
                    binding.cUserChangePassword.itemPassword2.requestFocus()
                }
                else -> {
                    changePassword(oldPassword, password, password2)
                }
            }
        }

        binding.cUserUpdate.btnSendCode.setOnClickListener {
            binding.progress.isVisible = true

            API.userSendCode(object : Callback<ApiError> {
                override fun onResponse(call: Call<ApiError>, response: Response<ApiError>) {
                    if (response.isSuccessful && response.body() != null) {
//                        Utils.showSnack(this@EditProfileActivity, response.body()!!.message)
                        showVerifiEmailDialog(response.body()!!.message)
                    } else {
                        val apiError = ApiError.fromResponseBody(response.errorBody())
                        if (apiError != null) {
                            Log.e("APIERROR", apiError.message)
                            Utils.showSnack(this@EditProfileActivity, apiError.message)
                        }
                    }
                    binding.progress.isVisible = false
                }

                override fun onFailure(call: Call<ApiError>, t: Throwable) {
                    Log.e("ERROR", "API.usersService().sendCode", t)
                    Utils.showSnack(this@EditProfileActivity, t.message.toString())
                    binding.progress.isVisible = false
                }
            })
        }

        binding.cUserUpdate.itemBirthdate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val dialog = DatePickerDialog.newInstance { _, year, monthOfYear, dayOfMonth ->
                    val month = if (monthOfYear < 10) "0$monthOfYear" else monthOfYear
                    val day = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth
                    binding.cUserUpdate.itemBirthdate.setText("$year-$month-$day")
                }
                dialog.show(supportFragmentManager, "Birthdate")
            }
        }

        componentActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    binding.progress.isVisible = true

                    val ruta = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor: Cursor? = contentResolver.query(
                        it.data!!.data!!, ruta, null, null, null
                    )
                    cursor!!.moveToFirst()
                    val picturePath = cursor.getString(cursor.getColumnIndexOrThrow(ruta[0]))
                    cursor.close()

                    val file = File(picturePath)
                    val mimeType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                    val fbody: RequestBody = file.asRequestBody(mimeType!!.toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("avatar", file.name, fbody)

                    API.userAvatar(filePart, object : Callback<AvatarResult> {
                        override fun onResponse(
                            call: Call<AvatarResult>, response: Response<AvatarResult>
                        ) {
                            if (response.isSuccessful) {
                                binding.itemImgProfile.loadImageProfile(response.body()!!.r500)
                            } else {
                                val apiError = ApiError.fromResponseBody(response.errorBody())
                                if (apiError != null) {
                                    Log.e("APIERROR", apiError.message)
                                    Utils.showSnack(this@EditProfileActivity, apiError.message)
                                }
                            }
                            binding.progress.isVisible = false
                        }

                        override fun onFailure(call: Call<AvatarResult>, t: Throwable) {
                            Log.e("ERROR", "API.usersService().changeAvatar", t)
                            Utils.showSnack(this@EditProfileActivity, t.message.toString())
                            binding.progress.isVisible = false
                        }
                    })
                } else {
                    binding.progress.isVisible = false
                }
            }
    }

    private fun changeWallet(wallet: String) {
        binding.cUserWallet.progressUserWallet.isVisible = true

        API.userWallet(wallet, object : Callback<ApiError> {
            override fun onResponse(call: Call<ApiError>, response: Response<ApiError>) {
                if (response.isSuccessful && response.body() != null) {
                    Utils.showSnack(this@EditProfileActivity, "Wallet cambiada")
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(this@EditProfileActivity, apiError.message)
                    }
                }
                binding.cUserWallet.progressUserWallet.isVisible = false
            }

            override fun onFailure(call: Call<ApiError>, t: Throwable) {
                Log.e("ERROR", "API.usersService().changeWallet", t)
                Utils.showSnack(this@EditProfileActivity, t.message.toString())
                binding.cUserWallet.progressUserWallet.isVisible = false
            }

        })
    }

    private fun update(
        firstName: String, lastName: String, email: String, phone: String,
        country: String, address: String, birthdate: String, identification: String
    ) {
        binding.cUserUpdate.progressUserUpdate.isVisible = true

        val userJson = UserJson(
            firstName, lastName, email, phone, country, address, birthdate, identification, user.sex
        )

        API.userUpdate(userJson, object : Callback<ApiError> {
            override fun onResponse(call: Call<ApiError>, response: Response<ApiError>) {
                if (response.isSuccessful && response.body() != null) {
                    var apiError = response.body()
                    if (apiError != null) {
                        if (email != this@EditProfileActivity.user.email) {
                            this@EditProfileActivity.user.is_verified = 0
                            binding.cUserUpdate.btnSendCode.isVisible = true
                        }

                        this@EditProfileActivity.user.let {
                            it.name = firstName
                            it.last_name = lastName
                            it.email = email
                            it.phone = phone
                            it.country = country
                            it.address = address
                            it.birthdate = birthdate
                            it.ci = identification
                        }

                        Preferences.saveCurrent(this@EditProfileActivity.user)
                        Utils.showSnack(this@EditProfileActivity, apiError.message)
                    } else {
                        apiError = ApiError.fromResponseBody(response.errorBody())
                        if (apiError != null) {
                            Log.e("APIERROR", apiError.message)
                            Utils.showSnack(this@EditProfileActivity, apiError.message)
                        }
                    }
                }
                binding.cUserUpdate.progressUserUpdate.isVisible = false
            }

            override fun onFailure(call: Call<ApiError>, t: Throwable) {
                Log.e("ERROR", "API.usersService().update", t)
                Utils.showSnack(this@EditProfileActivity, t.message.toString())
                binding.cUserUpdate.progressUserUpdate.isVisible = false
            }
        })
    }

    private fun changePassword(oldPassword: String, password: String, password2: String) {
        binding.cUserChangePassword.progressUserChangePassword.isVisible = true

        API.userChangePassword(oldPassword, password, password2, object : Callback<ApiError> {
            override fun onResponse(call: Call<ApiError>, response: Response<ApiError>) {
                if (response.isSuccessful && response.body() != null) {
                    Utils.showSnack(this@EditProfileActivity, "Contraseña modificada")
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(this@EditProfileActivity, apiError.message)
                    }
                }
                binding.cUserChangePassword.progressUserChangePassword.isVisible = false
            }

            override fun onFailure(call: Call<ApiError>, t: Throwable) {
                Log.e("ERROR", "API.usersService().changePassword", t)
                Utils.showSnack(this@EditProfileActivity, t.message.toString())
                binding.cUserChangePassword.progressUserChangePassword.isVisible = false
            }
        })
    }

    private fun changeAvatar() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            componentActivity.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    componentActivity.launch(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    )
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadProfile() {
        binding.progress.isVisible = true

        API.userCurrent(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        setProfile(userResponse)
                        Preferences.saveCurrent(userResponse)
                    }
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(this@EditProfileActivity, apiError.message)
                    }
                }
                binding.progress.isVisible = false
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("ERROR", "API.usersService().current", t)
                Utils.showSnack(this@EditProfileActivity, t.message.toString())
                binding.progress.isVisible = false
            }

        })
    }

    private fun setProfile(user: User) {
        this.user = user

        binding.cUserUpdate.let {
            it.itemFirstName.setText(user.name)
            it.itemLastName.setText(user.last_name)
            it.itemEmail.setText(user.email)
            it.itemPhone.setText(user.phone)
            it.spinnerSex.setSelection(if (user.sex == "M") 0 else 1)
            it.itemAddress.setText(user.address)
            it.itemBirthdate.setText(user.birthdate)
            it.itemIdentification.setText(user.ci)
            it.btnSendCode.isVisible = user.is_verified == 0

            it.spinnerCountryCode.setSelection(CountryCode.Country.indexOf(user.country))
        }

        binding.txtComments.text = user.comments.toString()
        binding.txtPosts.text = user.posts.toString()

        binding.txtBytesReceived.text = Utils.bytesToHuman(Preferences.bytesReceived)
        binding.txtBytesSend.text = Utils.bytesToHuman(Preferences.bytesSend)

        binding.cUserWallet.itemWallet.setText(user.ti_wallet)

        binding.itemImgProfile.loadImageProfile(user.avatar500())

        enableFields()
    }

    private fun showVerifiEmailDialog(message: String) {
        val dialogBinding = DialogVerifyEmailBinding.inflate(layoutInflater)
        val dialog = Utils.showDialog(this, dialogBinding.root)
        dialog.setCancelable(false)

        Utils.showSnack(dialogBinding.root, layoutInflater, message)

        dialogBinding.btCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btSubmit.setOnClickListener {
            val code = dialogBinding.etCode.text.toString().trim()

            if (code.isEmpty()) {
                Utils.showSnack(it, layoutInflater, "Codigo de verificacion vacio")
                return@setOnClickListener
            }

            dialogBinding.progressDialog.isVisible = true

            API.userVerify(code.toInt(), object : Callback<ApiError> {
                override fun onResponse(call: Call<ApiError>, response: Response<ApiError>) {
                    if (response.isSuccessful && response.body() != null) {
                        Utils.showSnack(this@EditProfileActivity, response.body()!!.message)
                        binding.cUserUpdate.btnSendCode.visibility = View.GONE
                        dialog.dismiss()
                    } else {
                        val apiError = ApiError.fromResponseBody(response.errorBody())
                        if (apiError != null) {
                            Log.e("APIERROR", apiError.message)
                            Utils.showSnack(it, layoutInflater, apiError.message)
                        }
                    }
                    dialogBinding.progressDialog.isVisible = false
                }

                override fun onFailure(call: Call<ApiError>, t: Throwable) {
                    Log.e("ERROR", "API.usersService().verify", t)
                    Utils.showSnack(it, layoutInflater, t.message.toString())
                    dialogBinding.progressDialog.isVisible = false
                }
            })
        }
        dialog.show()
    }

    private fun enableFields(enable: Boolean = true) {
        binding.fabChangeAvatar.isEnabled = enable

        binding.cUserUpdate.let {
            it.itemFirstName.isEnabled = enable
            it.itemLastName.isEnabled = enable
            it.itemEmail.isEnabled = enable
            it.itemPhone.isEnabled = enable
            it.spinnerSex.isEnabled = enable
            it.itemAddress.isEnabled = enable
            it.itemBirthdate.isEnabled = enable
            it.itemIdentification.isEnabled = enable
            it.btnSendCode.isEnabled = enable
            it.btnUpdateProfile.isEnabled = enable
            it.spinnerCountryCode.isEnabled = enable
        }

        binding.cUserWallet.let {
            it.itemWallet.isEnabled = enable
            it.btnChangeWallet.isEnabled = enable
        }

        binding.cUserChangePassword.let {
            it.itemOldPassword.isEnabled = enable
            it.itemPassword.isEnabled = enable
            it.itemPassword2.isEnabled = enable
            it.btnChangePassword.isEnabled = enable
        }
    }

}