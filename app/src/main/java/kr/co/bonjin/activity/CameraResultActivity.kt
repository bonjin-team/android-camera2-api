package kr.co.bonjin.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kr.co.bonjin.databinding.ActivityCameraResultBinding
import java.io.File

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
            val intent = Intent(this@CameraResultActivity, CameraActivity::class.java)
            intent.putExtra("imageData", file)
            setResult(1001, intent)
            finish()
        }

        runOnUiThread {
            Glide.with(this)
                .load(file)
                .into(binding.imageView)
        }
    }
}