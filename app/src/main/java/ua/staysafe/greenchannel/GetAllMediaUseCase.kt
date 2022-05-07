package ua.staysafe.greenchannel

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import java.util.*

class GetAllMediaUseCase {

    operator fun invoke(timestamp: Long, contentResolver: ContentResolver): List<FilesToDelete> {
        Log.e("mcheck", "today ${Date()} timestamp ${Date(timestamp)}")
        val list = mutableListOf<FilesToDelete>()
        val mTimeStamp = timestamp / 1000
        val imageQuery = FileQuery(FileSource.IMAGE, mTimeStamp)
        list.addAll(getFilesForSource(imageQuery, contentResolver))
        val videoQuery = FileQuery(FileSource.VIDEO, mTimeStamp)
        list.addAll(getFilesForSource(videoQuery, contentResolver))
        val audioQuery = FileQuery(FileSource.AUDIO, mTimeStamp)
        list.addAll(getFilesForSource(audioQuery, contentResolver))
        if (isQPlus()) {
            val downloadsQuery = FileQuery(FileSource.DOWNLOAD, mTimeStamp)
            list.addAll(getFilesForSource(downloadsQuery, contentResolver))
        }
        return list
    }

    private fun getFilesForSource(
        fileQuery: FileQuery,
        contentResolver: ContentResolver
    ): List<FilesToDelete> {
        Log.e("mcheck", "${"-".repeat(10)}${fileQuery.source}${"-".repeat(10)}")
        val list = mutableListOf<FilesToDelete>()

        try {
            fileQuery.collection.forEach { source ->
                Log.e("mcheck", "source $source")
                val query = contentResolver.query(
                    source, fileQuery.projection, fileQuery.selection, fileQuery.selectionArgs, null
                )
                query?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(fileQuery.projection[0])
                    val nameColumn = cursor.getColumnIndexOrThrow(fileQuery.projection[1])
                    val addColumn = cursor.getColumnIndexOrThrow(fileQuery.projection[2])
                    Log.e("mcheck", "cursor ${cursor.count}")
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn)
                        val dateAdded = cursor.getLong(addColumn)
                        val contentUri = ContentUris.withAppendedId(source, id)
                        val file = FilesToDelete(id, contentUri, name, dateAdded)
                        list.add(file)
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list.sortByDescending { it.dateAdded }
        val mapped = list.map { it.toString(fileQuery.timestamp) }
        Log.e("mcheck", "${mapped}")
        return list
    }

    private fun getVideos(timestamp: Long, contentResolver: ContentResolver): List<FilesToDelete> {
        Log.e("mcheck", "----Video Files----")
        val collection =
            if (isQPlus()) {
                listOf(
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_INTERNAL
                    ), MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    ), MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                )

            } else {
                listOf(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            }
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Video.Media.DATE_ADDED} >= ?"
        val selectionArgs = arrayOf(timestamp.toString())

        val list = mutableListOf<FilesToDelete>()
        collection.forEach { source ->
            val query = contentResolver.query(source, projection, selection, selectionArgs, null)
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val addColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                Log.e("mcheck", "cursor ${cursor.count}")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getLong(addColumn)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val file = FilesToDelete(id, contentUri, name, dateAdded)
                    list.add(file)
                }

            }
        }
        Log.e("mcheck", "${list.toString()}")
        return list
    }


}