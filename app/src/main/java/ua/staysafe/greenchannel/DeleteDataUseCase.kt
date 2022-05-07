package ua.staysafe.greenchannel

import android.content.ContentResolver

class DeleteDataUseCase {
    private val deleteFilesUseCase = DeleteFilesUseCase()
    private val deleteSmsUseCase = DeleteSmsUseCase()
    private val getAllMediaUseCase = GetAllMediaUseCase()
    operator fun invoke(timestamp: Long, contentResolver: ContentResolver) {
        val filesToDelete = getAllMediaUseCase.invoke(timestamp, contentResolver)
        deleteFilesUseCase.invoke(filesToDelete, contentResolver)
        deleteSmsUseCase.invoke(timestamp, contentResolver)
    }
}