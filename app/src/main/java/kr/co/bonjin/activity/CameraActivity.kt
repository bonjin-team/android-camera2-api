package kr.co.bonjin.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kr.co.bonjin.databinding.ActivityCameraBinding
import java.io.File
import java.io.FileOutputStream

class CameraActivity: AppCompatActivity()  {
    lateinit var binding: ActivityCameraBinding
    lateinit var cameraManager: CameraManager
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest
    lateinit var captureRequestBuilder: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var imageReader: ImageReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        performPermission()
    }

    /**
     * 실행 권한이 정상적으로 허용되면 초기화
     */
    private fun init() {
        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.takeButton.setOnClickListener {
            takePicture()
        }


        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        binding.textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
            /**
             * TextureView 객체가 화면에 정상적으로 나타나면 등록한 SurfaceTextureListener 객체의 onSerfaceTextureAvailable()메소드가 호출
             */
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }

        imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener({

            var image = it.acquireLatestImage()
            var buffer = image!!.planes.first().buffer
            var bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            var file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image.jpeg")
            var outputStream = FileOutputStream(file)
            outputStream.write(bytes)

            outputStream.close()
            image.close()

            Toast.makeText(this@CameraActivity, "이미지 캡쳐", Toast.LENGTH_SHORT).show()
        }, handler)
    }

    /**
     * 권한 요청
     */
    private fun performPermission() {
        val cameraCheck = ContextCompat.checkSelfPermission(this@CameraActivity, Manifest.permission.CAMERA)
        if(cameraCheck == PackageManager.PERMISSION_DENIED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA), 1
            )
        } else {
            init()
        }
    }

    /**
     * CameraManager Init
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList.first(), object: CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera

                var surface = Surface(binding.textureView.surfaceTexture)
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequestBuilder.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                    }

                },  handler)
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }

        }, handler)
    }

    private fun takePicture() {
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(imageReader.surface)
        cameraCaptureSession.capture(captureRequestBuilder.build(), null, null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1) {
            init()
        } else {
            Toast.makeText(this,
                "권한을 허용해주세요",
                Toast.LENGTH_LONG).show();
            finish()
        }
    }
}