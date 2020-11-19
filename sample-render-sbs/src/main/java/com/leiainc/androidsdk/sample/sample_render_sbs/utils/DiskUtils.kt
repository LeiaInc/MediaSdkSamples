package com.leiainc.androidsdk.sample.sample_render_sbs.utils

import android.content.Context
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.RawRes
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object DiskUtils {

    fun saveResourceToFile(context: Context, @RawRes defaultImage: Int): Uri? {
        val testFile = File(
            context.externalCacheDir, getFileNameFromResource(context, defaultImage)
        )
        try {
            createFile(context, testFile.path, defaultImage)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.fromFile(testFile)
    }

    /**
     * a function to create a file and move the content in the resource into the file.
     *
     * @param outputFile field to be created and outputted.
     * @param resource resource object which contains the content of the file
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createFile(context: Context, outputFile: String, resource: Int) {
        val outputStream: OutputStream = FileOutputStream(outputFile)
        val largeBuffer = ByteArray(1024 * 4)
        var bytesRead: Int
        val inputStream = context.resources.openRawResource(resource)
        while (inputStream.read(largeBuffer).also { bytesRead = it } > 0) {
            if (largeBuffer.size == bytesRead) {
                outputStream.write(largeBuffer)
            } else {
                val shortBuffer = ByteArray(bytesRead)
                System.arraycopy(largeBuffer, 0, shortBuffer, 0, bytesRead)
                outputStream.write(shortBuffer)
            }
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    }

    private fun getFileNameFromResource(context: Context, @RawRes resource: Int): String? {
        val typedValue = TypedValue()
        context.resources.getValue(resource, typedValue, true)
        val fileNameArray = typedValue.string.toString().split("/".toRegex()).toTypedArray()
        return fileNameArray[fileNameArray.size - 1]
    }
}