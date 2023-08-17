package kr.co.bonjin.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.bonjin.databinding.ActivityCameraResultBinding
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
        var file: File? = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("imageData", File::class.java)
        } else {
            intent.getSerializableExtra("imageData") as File
        }

        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.successButton.setOnClickListener {
            val cropped: Bitmap? = binding.cropImageView.getCroppedImage()
            val byteOutput = ByteArrayOutputStream()
            cropped?.compress(Bitmap.CompressFormat.JPEG, 70, byteOutput)
            val bytes = byteOutput.toByteArray()
            val base64String = Base64.getEncoder().encodeToString(bytes)

            val intent = Intent(this@CameraResultActivity, CameraActivity::class.java)
            intent.putExtra("imageData", base64String)
            setResult(1001, intent)
            finish()
        }

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        val decodeStream = BitmapFactory.decodeStream(FileInputStream(file), null, options)
        binding.cropImageView.setImageBitmap(decodeStream)
    }
}