package kr.co.bonjin.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kr.co.bonjin.MainActivity
import kr.co.bonjin.databinding.ActivityCameraBinding
import kr.co.bonjin.utils.FileUtil
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale

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
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

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

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 1001) {
                val intent = result.data
                var imageFilePath: String? = intent?.getStringExtra("imageFilePath")
                val newIntent = Intent(this@CameraActivity, MainActivity::class.java)
                newIntent.putExtra("imageFilePath", imageFilePath)
                setResult(1001, newIntent)
                finish()
            }
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
            val now = Date()
            val time: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(now)

            var image = it.acquireLatestImage()
            var buffer = image!!.planes.first().buffer
            var bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val file = FileUtil.saveByteArrayToFileInTempDir(bytes, "jpg")
            var intent = Intent(this@CameraActivity, CameraResultActivity::class.java)
            intent.putExtra("imageDataPath", file.absolutePath);
            activityResultLauncher.launch(intent)
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
        val rotation = windowManager.defaultDisplay.rotation
        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

        cameraCaptureSession.capture(captureRequestBuilder.build(), null, null)
    }

    private fun getOrientation(rotation: Int): Int {
        val sensorOrientation = cameraManager.getCameraCharacteristics(cameraManager.cameraIdList.first())
            .get(CameraCharacteristics.SENSOR_ORIENTATION)
        if (sensorOrientation != null) {
            return (sensorOrientation + ORIENTATIONS.get(rotation) + 270) % 360
        }
        return 0
    }

    private val ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 0)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 180)
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