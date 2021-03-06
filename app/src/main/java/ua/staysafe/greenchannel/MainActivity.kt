package ua.staysafe.greenchannel

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ua.staysafe.greenchannel.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    lateinit var binding: ActivityMainBinding

    companion object {
        const val PREFFS = "preffs"
        const val PREFFS_DEFAULT = "default_app"
        const val PREFFS_TIME = "time"
        const val MAKE_DEFAULT_APP_REQUEST = 1144
        const val PERMISSION_SEND_SMS = 1155

        const val REQUEST_FILE_MANAGE = 1011
    }

    lateinit var preffs: SharedPreferences
    var timestamp = 0L
    val df = SimpleDateFormat("dd/MM/yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preffs = this.getSharedPreferences(PREFFS, MODE_PRIVATE)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val defaultPackage = Telephony.Sms.getDefaultSmsPackage(this)
        val my = this.packageName
        Log.e("mcheck", "$defaultPackage $my ${defaultPackage == my}")
        binding.btnRegister.setOnClickListener { makeMeDefaultAppRequest(packageName) }
        binding.btnHide.setOnClickListener { checkStoragePermissionAndDelete() }
        binding.btnRestoreDefault.setOnClickListener {
            val prev = preffs.getString(PREFFS_DEFAULT, "")
            makeMeDefaultAppRequest(prev!!)
        }
        binding.btnUninstall.setOnClickListener { deleteGreenChannel() }
        binding.hCard.setOnClickListener { selectDate() }
        timestamp = preffs.getLong(PREFFS_TIME, 0L)
        if (timestamp == 0L) {
            timestamp = df.parse("24/02/2022").time
        }

    }


    override fun onResume() {
        super.onResume()
        updatePreviousApp()
        updateUi()
    }


    private fun selectDate() {
        val c = Calendar.getInstance()
        c.timeInMillis = timestamp
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val dayOfMonth = c.get(Calendar.DAY_OF_MONTH)

        val picker = DatePickerDialog(this, 0, this, year, month, dayOfMonth)
        picker.datePicker.maxDate = System.currentTimeMillis()
        picker.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        c.clear()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        timestamp = c.timeInMillis
        preffs.edit().putLong(PREFFS_TIME, timestamp).apply()
        updateDate()
    }

    private fun deleteGreenChannel() {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun updatePreviousApp() {
        val defaultPackage = Telephony.Sms.getDefaultSmsPackage(this)
        val my = this.packageName
        if (defaultPackage != my) {
            preffs.edit().putString(PREFFS_DEFAULT, defaultPackage).apply()
        }
    }

    private fun updatePreviousName() {
        val name = "Previous sms app was: ${getAppNameFromPackage()}"
        binding.tvPrevious.text = name

    }

    private fun updateDate() {
        binding.tvDate.text = df.format(timestamp)
    }

    private fun getAppNameFromPackage(): String {
        val pm = this.packageManager
        val info = try {
            val prev = preffs.getString(PREFFS_DEFAULT, "")
            pm.getApplicationInfo(prev!!, 0)
        } catch (e: Exception) {
            null
        }
        return info?.let { pm.getApplicationLabel(it).toString() } ?: "Error"
    }

    private fun updateUi() {
        val isDefault = isDefaultSmsApp(this)
        with(binding) {
            btnRegister.isVisible = !isDefault
            btnHide.isVisible = isDefault
            btnRestoreDefault.isVisible = isDefault
            tvSince.isVisible = isDefault
            tvPrevious.isVisible = isDefault
            hCard.isVisible = isDefault
            updatePreviousName()
            updateDate()
        }
    }


    private fun isDefaultSmsApp(context: Context): Boolean {
        return context.packageName == (Telephony.Sms.getDefaultSmsPackage(context))
    }

    private fun makeMeDefaultAppRequest(packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    if (packageName != this.packageName) {
                        val i = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                        startActivity(i)
                    } else {
                    }
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    startActivityForResult(intent, MAKE_DEFAULT_APP_REQUEST)
                }
            } else {
                Log.e("mcheck", "some error")

            }
        } else {
            if (Telephony.Sms.getDefaultSmsPackage(this) == packageName) {
                if (packageName != this.packageName) {
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                    startActivityForResult(intent, MAKE_DEFAULT_APP_REQUEST)
                } else {
                }
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivityForResult(intent, MAKE_DEFAULT_APP_REQUEST)
            }
        }
    }

    private fun requestPermissionsOrDelete() {
        Log.e("mcheck", "requestPermissionsOrDelete")

        // check permission is given
        val list = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
//        if (isQPlus()) {
//            list.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
//        }
        val check = list
            .map {
                val isGranted =
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                Log.e("mcheck", "$it isGranted:$isGranted")
                isGranted
            }.all { it }
        Log.e("mcheck", "all permissions granted $check")
        if (!check) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(this, list.toTypedArray(), PERMISSION_SEND_SMS)
        } else {
            // permission already granted run sms send
            deleteData()
        }
    }

    @SuppressLint("NewApi")
    private fun checkStoragePermissionAndDelete() {
        if (isRPlus()) {
            if (Environment.isExternalStorageManager()) {
                requestPermissionsOrDelete()

            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_FILE_MANAGE)
            }
        } else {
            requestPermissionsOrDelete()
        }
    }

    private fun deleteData() {
        DeleteDataUseCase().invoke(timestamp, contentResolver)
    }


}