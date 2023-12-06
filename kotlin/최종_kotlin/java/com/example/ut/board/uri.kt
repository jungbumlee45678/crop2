package com.example.ut.board

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore

class uri {
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        when {
            // MediaStore (and general)
            "content" == contentUri.scheme!!.lowercase() -> {
                val cursor = context.contentResolver.query(contentUri, projection, null, null, null)
                cursor?.use {
                    it.moveToFirst()
                    val columnIndex = it.getColumnIndex(projection[0])
                    filePath = it.getString(columnIndex)
                }
            }
            // File
            "file" == contentUri.scheme!!.lowercase() -> {
                filePath = contentUri.path
            }
            // Documents
            "content" == contentUri.scheme!!.lowercase() -> {
                val documentId = DocumentsContract.getDocumentId(contentUri)
                when {
                    isMediaDocument(contentUri) -> {
                        val id = documentId.split(":")[1]
                        val selection = MediaStore.Images.Media._ID + "=?"
                        val selectionArgs = arrayOf(id)
                        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        val projection = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)
                        cursor?.use {
                            it.moveToFirst()
                            val columnIndex = it.getColumnIndex(projection[0])
                            filePath = it.getString(columnIndex)
                        }
                    }
                    isDownloadsDocument(contentUri) -> {
                        val id = documentId.split(":")[1]
                        val contentUri = Uri.parse("content://downloads/public_downloads")
                        val selection = MediaStore.Images.Media._ID + "=?"
                        val selectionArgs = arrayOf(id)
                        val projection = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)
                        cursor?.use {
                            it.moveToFirst()
                            val columnIndex = it.getColumnIndex(projection[0])
                            filePath = it.getString(columnIndex)
                        }
                    }
                    isExternalStorageDocument(contentUri) -> {
                        val id = documentId.split(":")[1]
                        val parts = id.split("/")
                        if (parts.size >= 2) {
                            val first = parts[0]
                            val second = parts[1]
                            val baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(first).build()
                            val selection = "_id=?"
                            val selectionArgs = arrayOf(second)
                            val projection = arrayOf(MediaStore.Images.Media.DATA)
                            val cursor = context.contentResolver.query(baseUri, projection, selection, selectionArgs, null)
                            cursor?.use {
                                it.moveToFirst()
                                val columnIndex = it.getColumnIndex(projection[0])
                                filePath = it.getString(columnIndex)
                            }
                        }
                    }
                }
            }
        }
        return filePath
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}