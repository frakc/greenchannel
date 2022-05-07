package ua.staysafe.greenchannel

import android.net.Uri
import java.util.*

data class FilesToDelete(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateAdded: Long
){
    fun toString(timeStamp:Long): String {
        return "$name added date ${Date(dateAdded*1000)} $dateAdded timestamp $timeStamp isNewer ${dateAdded>timeStamp}"
    }
}