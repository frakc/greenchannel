package ua.staysafe.greenchannel

import android.net.Uri
import android.provider.MediaStore

class FileQuery(val source: FileSource,  val timestamp: Long) {
    val collection: List<Uri>
    val projection: Array<String>
    val selection: String
    var selectionArgs = arrayOf(timestamp.toString())

    init {
        collection = getCollection(source)
        projection = getProjection(source)
        selection = getSelection(source)
    }

//    @SuppressLint("NewApi")
//    fun getUri(id: Long): Uri {
//        val location = when (source) {
//            FileSource.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            FileSource.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            FileSource.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//            FileSource.DOWNLOAD -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
//        }
//        return ContentUris.withAppendedId(location, id)
//    }

    private fun getSelection(source: FileSource): String {
        return when (source) {
            FileSource.VIDEO -> {
                selectionArgs =
                    arrayOf(timestamp.toString(), timestamp.toString(), timestamp.toString())
                "${MediaStore.Video.Media.DATE_ADDED} >= ? OR ${MediaStore.Video.Media.DATE_TAKEN} >= ? OR ${MediaStore.Video.Media.DATE_MODIFIED} >= ? "
            }
            FileSource.IMAGE -> {
                selectionArgs =
                    arrayOf(timestamp.toString(), timestamp.toString(), timestamp.toString())
                "${MediaStore.Images.Media.DATE_ADDED} >= ? OR ${MediaStore.Images.Media.DATE_TAKEN} >= ? OR ${MediaStore.Images.Media.DATE_MODIFIED} >= ? "
            }
            FileSource.AUDIO -> {
                if (isQPlus()) {
                    selectionArgs =
                        arrayOf(timestamp.toString(), timestamp.toString(), timestamp.toString())
                    "${MediaStore.Audio.Media.DATE_ADDED} >= ? OR ${MediaStore.Audio.Media.DATE_TAKEN} >= ? OR ${MediaStore.Audio.Media.DATE_MODIFIED} >= ? "
                } else {
                    selectionArgs = arrayOf(timestamp.toString(), timestamp.toString())
                    "${MediaStore.Audio.Media.DATE_ADDED} >= ? OR  ${MediaStore.Audio.Media.DATE_MODIFIED} >= ? "

                }
            }
            FileSource.DOWNLOAD -> {
                if (isQPlus()) {
                    selectionArgs =
                        arrayOf(timestamp.toString(), timestamp.toString(), timestamp.toString())
                    "${MediaStore.Downloads.DATE_ADDED} >= ? OR ${MediaStore.Downloads.DATE_TAKEN} >= ? OR ${MediaStore.Downloads.DATE_MODIFIED} >= ? "
                } else {
                    selectionArgs = arrayOf(timestamp.toString(), timestamp.toString())
                    "${MediaStore.Downloads.DATE_ADDED} >= ? OR  ${MediaStore.Downloads.DATE_MODIFIED} >= ? "
                }
            }
        }
    }

    private fun getProjection(source: FileSource): Array<String> {

        return when (source) {
            FileSource.VIDEO -> arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
            )
            FileSource.IMAGE -> arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )
            FileSource.AUDIO -> arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_ADDED
            )
            FileSource.DOWNLOAD -> arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.DATE_ADDED
            )
        }
    }

    private fun getCollection(source: FileSource): List<Uri> {
        return when (source) {
            FileSource.VIDEO -> {
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
            }
            FileSource.IMAGE -> {
                if (isQPlus()) {
                    listOf(
                        MediaStore.Images.Media.getContentUri(
                            MediaStore.VOLUME_INTERNAL
                        ), MediaStore.Images.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL
                        ), MediaStore.Images.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    )

                } else {
                    listOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                }
            }
            FileSource.AUDIO -> {
                if (isQPlus()) {
                    listOf(
                        MediaStore.Audio.Media.getContentUri(
                            MediaStore.VOLUME_INTERNAL
                        ), MediaStore.Audio.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL
                        ), MediaStore.Audio.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    )

                } else {
                    listOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                }
            }
            FileSource.DOWNLOAD -> {
                if (isQPlus()) {
                    listOf(
                        MediaStore.Downloads.getContentUri(
                            MediaStore.VOLUME_INTERNAL
                        ), MediaStore.Downloads.getContentUri(
                            MediaStore.VOLUME_EXTERNAL
                        ), MediaStore.Downloads.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    )

                } else {
                    listOf(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
                }
            }
        }
    }
}