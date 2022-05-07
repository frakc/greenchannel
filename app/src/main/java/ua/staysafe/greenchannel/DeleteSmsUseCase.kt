package ua.staysafe.greenchannel

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.util.Log

class DeleteSmsUseCase {
    operator fun invoke(timestamp: Long, contentResolver: ContentResolver) {
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
                } while (c.moveToNext())
            }
        } catch (e: java.lang.Exception) {
        }
    }

}