package kr.co.bonjin.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.co.bonjin.MainActivity
import kr.co.bonjin.databinding.ActivityCameraBinding
import kr.co.bonjin.utils.FileUtil


class CameraActivity: AppCompatActivity()  {
    lateinit var binding: ActivityCameraBinding
    private lateinit var cameraManager: CameraManager
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequestBuilder: CaptureRequest.Builder
    lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    lateinit var imageReader: ImageReader
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    /**
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
                val imageFilePath: String? = intent?.getStringExtra("imageFilePath")
                val newIntent = Intent(this@CameraActivity, MainActivity::class.java)
                newIntent.putExtra("imageFilePath", imageFilePath)
                setResult(1001, newIntent)
                finish()
            }
        }

        initCamera()

        performPermission()
    }

    private fun initCamera() {
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
                adjustTextureViewSize(width,height)
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

        imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1) //캡쳐 후 크기?
        imageReader.setOnImageAvailableListener({

            val image = it.acquireLatestImage()
            val buffer = image!!.planes.first().buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            image.close()

            val file = FileUtil.saveByteArrayToFileInTempDir(bytes, "jpg")
            val intent = Intent(this@CameraActivity, CameraResultActivity::class.java)
            intent.putExtra("imageDataPath", file.absolutePath)
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
            initCamera()
        }
    }

    /**
     * CameraManager Init
     */
    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        cameraManager.openCamera(cameraManager.cameraIdList.first(), object: CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                val surface = Surface(binding.textureView.surfaceTexture)
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

    /**
     * 촬영모드 화면 비율 설정
     */
    private fun adjustTextureViewSize(viewWidth: Int, viewHeight: Int) {
        // 원하는 화면 비율 설정
        var desiredWidth = viewWidth
        var desiredHeight = viewWidth * 1 / 1 // 1:1 비율
        if (desiredHeight > viewHeight) {
            // 뷰의 높이에 맞추기 위해 너비 조정
            desiredWidth = viewHeight * 1 / 1
            desiredHeight = viewHeight
        }
        val params: ViewGroup.LayoutParams = binding.textureView.layoutParams
        params.width = desiredWidth
        params.height = desiredHeight
        binding.textureView.layoutParams = params
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
            openCamera()
        } else {
            Toast.makeText(this,
                "권한을 허용해주세요",
                Toast.LENGTH_LONG).show()
            finish()
        }
    }
}