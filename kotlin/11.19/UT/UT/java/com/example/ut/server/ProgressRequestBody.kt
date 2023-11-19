package com.example.ut.server

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import okio.buffer
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType,
    private val onProgress: (progress: Float) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        return file.length()
    }

    override fun writeTo(sink: BufferedSink) {
        val source = file.source()
        val bufferedSource = source.buffer()

        var totalBytesRead: Long = 0
        var bytesRead: Long
        while (bufferedSource.read(sink.buffer, SEGMENT_SIZE.toLong()).also { bytesRead = it } != -1L) {
            totalBytesRead += bytesRead
            sink.flush()
            // 진행률을 계산하고 콜백으로 전달
            onProgress(totalBytesRead.toFloat() / contentLength())
        }

        bufferedSource.close()
    }

    companion object {
        private const val SEGMENT_SIZE = 2048
    }
}
