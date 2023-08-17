package kr.co.bonjin.utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    /**
     * saveBitmapToFile(bitmap, tempDir, fileExtension)
     */
    fun saveBitmapToFile(bitmap: Bitmap, tempDir: File, fileExtension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Temp_${timeStamp}.$fileExtension"
        val tempFile = File(tempDir, fileName)

        val outputStream = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        return tempFile
    }
    /**
     * createTempFileInTempDir(tempDir, "jpg")
     */
    fun createTempFileInTempDir(tempDir: File, fileExtension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Temp_${timeStamp}.$fileExtension"
        return File(tempDir, fileName)
    }

    /**
     * saveByteArrayToFileInTempDir(byteArray, 'jpg')
     */
    fun saveByteArrayToFileInTempDir(byteArray: ByteArray, fileExtension: String): File {
        val tempDir = File.createTempFile("tempDir", null).apply { delete() }
        tempDir.mkdir()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Temp_${timeStamp}.$fileExtension"
        val tempFile = File(tempDir, fileName)

        tempFile.outputStream().use { outputStream ->
            outputStream.write(byteArray)
        }
        return tempFile
    }

    /**
     * getBase64FromFile(tempFile)
     */
    fun getBase64FromFile(file: File): String {
        val bytes = file.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * getBase64FromFilePath(tempFilePath)
     */
    fun getBase64FromFilePath(filePath: String): String {
        val file = File(filePath)
        val bytes = file.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }
}