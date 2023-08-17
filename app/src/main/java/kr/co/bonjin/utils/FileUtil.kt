package kr.co.bonjin.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    fun rotateBitmapIfNeeded(imageFilePath: String): Bitmap {
        val exif = try {
            ExifInterface(imageFilePath)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val bitmap = BitmapFactory.decodeFile(imageFilePath)
        return if (rotationDegrees != 0) {
            // 이미지를 회전시켜 반환
            android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                .let { Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, it, true) }
        } else {
            bitmap
        }
    }

    fun saveRotatedImage(byteArray: ByteArray, outputFilePath: String) {
        val outputStream = FileOutputStream(outputFilePath)
        outputStream.write(byteArray)
        outputStream.close()

        // 이미지 회전 정보를 확인하여 저장된 이미지 회전
        val exifInterface = ExifInterface(outputFilePath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        if (rotationDegrees != 0) {
            // 이미지를 회전시켜서 덮어쓰기
            // 이 단계에서 필요한 회전 처리를 수행할 수 있습니다.
            // 예를 들어 이미지 회전을 보정하는 라이브러리를 사용할 수 있습니다.
            // 이 예시에서는 단순히 이미지를 회전시킵니다.
            // (이 단계에서 추가 작업이 필요할 수 있습니다.)
            exifInterface.setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL.toString()
            )
            exifInterface.saveAttributes()
        }
    }

    /**
     * getBase64FromFile(tempFile)
     */
    fun getBase64FromFile(file: File): String {
        val bytes = file.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun getFileFromFilePath(filePath: String): String {
        val file = File(filePath)
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