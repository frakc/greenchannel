package ua.staysafe.greenchannel

import android.Manifest
import android.app.DatePickerDialog
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ua.staysafe.greenchannel.databinding.ActivityMainBinding
import java.io.File
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
        binding.btnHide.setOnClickListener { requestPermissionsOrDelete() }
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

        // check permission is given
        val list = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val check = list
            .map {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }.all { it }
        Log.e("mcheck", "check $check")
        if (!check) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(this, list, PERMISSION_SEND_SMS)
        } else {
            // permission already granted run sms send
            deleteData()
        }
    }

    private fun deleteData() {
        try {
            deleteFiles()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        deleteSMS()
    }

    private fun deleteFiles() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val projecitons = arrayOf("_id", "_display_name", "datetaken")

            val result = resolver.delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "datetaken > ?",
                arrayOf(timestamp.toString())
            )
            Log.e("mcheck", "result $result")
            val cursor: Cursor? =
                resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projecitons, null, null, null)
            Log.e("mcheck", "columns ${cursor?.columnNames?.toString()}")
//            cursor?.columnNames?.forEach {
//                Log.e("mcheck","columns $it")
//
//            }
            cursor?.let { c ->
                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.)
//                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                if (c.moveToFirst()) {
                    do {

                        val id = cursor.getLong(0)
                        val title = cursor.getString(nameColumnIndex)
                        val date = cursor.getLong(2)
                        var meta = ""
                        c.columnNames.forEachIndexed { index, s ->
                            try {

                                val content = c.getString(index)
                                if (content != null && content != "null") {
                                    meta += "[$s $content]"
                                }
                            } catch (e: Exception) {
                                meta += "[$s error]"
                            }
                        }
                        Log.e("mcheck", "meta $meta")

                        Log.e("mcheck", "file $id, $title, $date")
                    } while (c.moveToNext())

                } else {
                    Log.e("mcheck", "cannot move to first")

                }

            }

        } else {
            Log.e("mheck", "delete data ")
            val externalLocks = listOf(
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_DOCUMENTS,
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_PICTURES
            )
            externalLocks.forEach {


                val externalDir = this.getExternalFilesDir(it)
                traverseRootDir(externalDir)
            }
        }
    }

    private fun delteImages() {
        val resolver = contentResolver
        val projecitons = arrayOf("_id", "_display_name", "datetaken")
        val cursor: Cursor? =
            resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null)
        Log.e("mcheck", "columns ${cursor?.columnNames?.toString()}")
//            cursor?.columnNames?.forEach {
//                Log.e("mcheck","columns $it")
//
//            }
        cursor?.let { c ->
            val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.)
//                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (c.moveToFirst()) {
                do {

                    val id = cursor.getLong(0)
                    val title = cursor.getString(nameColumnIndex)
                    val date = cursor.getLong(2)
                    var meta = ""
                    c.columnNames.forEachIndexed { index, s ->
                        try {

                            val content = c.getString(index)
                            if (content != null && content != "null") {
                                meta += "[$s $content]"
                            }
                        } catch (e: Exception) {
                            meta += "[$s error]"
                        }
                    }
                    Log.e("mcheck", "meta $meta")

                    Log.e("mcheck", "file $id, $title, $date")
                } while (c.moveToNext())

            } else {
                Log.e("mcheck", "cannot move to first")

            }

        }
    }

    private fun traverseRootDir(dir: File?) {
        Log.e("mcheck", "dir ${dir?.name} ${dir?.path}")
        if (dir != null && dir.exists() && dir.isDirectory) {
            val fileList = dir.listFiles()
            Log.e("mcheck", "list ${fileList.map { it.name }}")

            fileList?.forEach { deleteRecursive(it) }
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        if (fileOrDirectory.lastModified() > timestamp) {
            Log.e("mcheck", "file to delete ${fileOrDirectory.name}")
            fileOrDirectory.delete()
        } else {
            Log.e("mcheck", "file skipped ${fileOrDirectory.name}")

        }
    }

    private fun deleteSMS() {
        try {
            val uriSms = Uri.parse("content://sms/")
            contentResolver.delete(uriSms, "date > ?", arrayOf(timestamp.toString()))
            val c: Cursor? = contentResolver.query(
                uriSms, arrayOf("_id", "date"), "date > ?", arrayOf(timestamp.toString()), null
            )
            if (c != null && c.moveToFirst()) {
                do {
                    val id = c.getLong(0)
                    val date = c.getLong(1)
                    Log.e("mcheck", "sms $id $date")
//                    val id: Long = c.getLong(0)
//                    val threadId: Long = c.getLong(1)
//                    val address: String = c.getString(2)
//                    val body: String = c.getString(5)
//                    if (message == body && address == number) {
//                        mLogger.logInfo("Deleting SMS with id: $threadId")
//                        context.contentResolver.delete(
//                            Uri.parse("content://sms/$id"), null, null
//                        )
//                    }
                } while (c.moveToNext())
            }
        } catch (e: java.lang.Exception) {
        }
    }
}