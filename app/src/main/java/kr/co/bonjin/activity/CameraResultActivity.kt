package kr.co.bonjin.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.bonjin.databinding.ActivityCameraResultBinding
import kr.co.bonjin.utils.FileUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.Base64

class CameraResultActivity: AppCompatActivity(){
    lateinit var binding: ActivityCameraResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        var imageFilePath: String? = intent.getStringExtra("imageDataPath")
        imageFilePath?.let {
            val bitmap = FileUtil.rotateBitmapIfNeeded(imageFilePath)
            binding.cropImageView.setImageBitmap(bitmap)
        }

        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.successButton.setOnClickListener {
            val cropped: Bitmap? = binding.cropImageView.getCroppedImage()
            val byteOutput = ByteArrayOutputStream()
            cropped?.compress(Bitmap.CompressFormat.JPEG, 70, byteOutput)

            cropped?.let {
                val file = FileUtil.saveBitmapToFile(cropped, this.cacheDir, "jpg")

                val intent = Intent(this@CameraResultActivity, CameraActivity::class.java)
                intent.putExtra("imageFilePath", file.absolutePath)
                setResult(1001, intent)
                finish()
            }
        }


    }
}