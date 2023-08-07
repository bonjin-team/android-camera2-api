package kr.co.bonjin.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kr.co.bonjin.databinding.ActivityCameraBinding

class CameraActivity: AppCompatActivity(), SurfaceHolder.Callback {
    lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {

        binding.closeButton.setOnClickListener {
            finish()
        }

        val cameraCheck = ContextCompat.checkSelfPermission(this@CameraActivity, Manifest.permission.CAMERA)
        if(cameraCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA), 1
            )
        }
    }

    private fun openCamera() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1) {

        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }
}