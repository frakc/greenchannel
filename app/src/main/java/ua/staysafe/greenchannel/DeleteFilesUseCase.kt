package ua.staysafe.greenchannel

import android.content.ContentResolver
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.File

class DeleteFilesUseCase {
    operator fun invoke(filesToDelete: List<FilesToDelete>, contentResolver: ContentResolver) {
        var deleted = 0
        filesToDelete.forEach {
            val result = contentResolver.delete(it.uri,null,null)
            if(result==1) deleted++
        }
        Log.e("mcheck", "had ${filesToDelete.size} deleted $deleted")
//        if (isQPlus()) {
//            val resolver = contentResolver
//            val projecitons = arrayOf("_id", "_display_name", "datetaken")
//
//            val result = resolver.delete(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "datetaken > ?",
//                arrayOf(timestamp.toString())
//            )
//            Log.e("mcheck", "result $result")
//            val cursor: Cursor? =
//                resolver.query(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    projecitons,
//                    null,
//                    null,
//                    null
//                )
//            Log.e("mcheck", "columns ${cursor?.columnNames?.toString()}")
////            cursor?.columnNames?.forEach {
////                Log.e("mcheck","columns $it")
////
////            }
//            cursor?.let { c ->
//                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
////                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.)
////                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//
//                if (c.moveToFirst()) {
//                    do {
//
//                        val id = cursor.getLong(0)
//                        val title = cursor.getString(nameColumnIndex)
//                        val date = cursor.getLong(2)
//                        var meta = ""
//                        c.columnNames.forEachIndexed { index, s ->
//                            try {
//
//                                val content = c.getString(index)
//                                if (content != null && content != "null") {
//                                    meta += "[$s $content]"
//                                }
//                            } catch (e: Exception) {
//                                meta += "[$s error]"
//                            }
//                        }
//                        Log.e("mcheck", "meta $meta")
//
//                        Log.e("mcheck", "file $id, $title, $date")
//                    } while (c.moveToNext())
//
//                } else {
//                    Log.e("mcheck", "cannot move to first")
//
//                }
//
//            }
//
//        } else {
//            Log.e("mheck", "delete data ")
//            val externalLocks = listOf(
//                Environment.DIRECTORY_DCIM,
//                Environment.DIRECTORY_DOCUMENTS,
//                Environment.DIRECTORY_DOWNLOADS,
//                Environment.DIRECTORY_MOVIES,
//                Environment.DIRECTORY_MUSIC,
//                Environment.DIRECTORY_PICTURES
//            )
//            externalLocks.forEach {
//
//
////                val externalDir = this.getExternalFilesDir(it)
////                traverseRootDir(externalDir)
//            }
//        }
    }
//    private fun delteImages() {
//        val resolver = contentResolver
//        val projecitons = arrayOf("_id", "_display_name", "datetaken")
//        val cursor: Cursor? =
//            resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null)
//        Log.e("mcheck", "columns ${cursor?.columnNames?.toString()}")
////            cursor?.columnNames?.forEach {
////                Log.e("mcheck","columns $it")
////
////            }
//        cursor?.let { c ->
//            val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
////                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.)
////                val nameColumnIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//
//            if (c.moveToFirst()) {
//                do {
//
//                    val id = cursor.getLong(0)
//                    val title = cursor.getString(nameColumnIndex)
//                    val date = cursor.getLong(2)
//                    var meta = ""
//                    c.columnNames.forEachIndexed { index, s ->
//                        try {
//
//                            val content = c.getString(index)
//                            if (content != null && content != "null") {
//                                meta += "[$s $content]"
//                            }
//                        } catch (e: Exception) {
//                            meta += "[$s error]"
//                        }
//                    }
//                    Log.e("mcheck", "meta $meta")
//
//                    Log.e("mcheck", "file $id, $title, $date")
//                } while (c.moveToNext())
//
//            } else {
//                Log.e("mcheck", "cannot move to first")
//
//            }
//
//        }
//    }

//    private fun traverseRootDir(dir: File?) {
//        Log.e("mcheck", "dir ${dir?.name} ${dir?.path}")
//        if (dir != null && dir.exists() && dir.isDirectory) {
//            val fileList = dir.listFiles()
//            Log.e("mcheck", "list ${fileList.map { it.name }}")
//
//            fileList?.forEach { deleteRecursive(it) }
//        }
//    }
//
//    private fun deleteRecursive(fileOrDirectory: File) {
//        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
//            child
//        )
//        if (fileOrDirectory.lastModified() > timestamp) {
//            Log.e("mcheck", "file to delete ${fileOrDirectory.name}")
//            fileOrDirectory.delete()
//        } else {
//            Log.e("mcheck", "file skipped ${fileOrDirectory.name}")
//
//        }
//    }
//    private fun deleteFiles() {
//
//
//    }

}