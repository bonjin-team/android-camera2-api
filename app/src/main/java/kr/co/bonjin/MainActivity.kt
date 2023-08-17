package kr.co.bonjin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kr.co.bonjin.activity.CameraActivity
import kr.co.bonjin.databinding.ActivityMainBinding
import kr.co.bonjin.utils.FileUtil

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.webview.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
        }
        binding.webview.addJavascriptInterface(WebViewJavascriptInterface(this), "android")
        binding.webview.loadUrl("https://gigas.synology.me/bonjin/android/camera2api")
        binding.webview.clearCache(true)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 1001) {
                var imageFilePath: String? = result.data?.getStringExtra("imageFilePath")
                imageFilePath?.let {
                    val base64String = FileUtil.getBase64FromFilePath(imageFilePath)
                    binding.webview.loadUrl("javascript:resultCamera('$base64String');")
                }
            }
        }
    }

    inner class WebViewJavascriptInterface(private val mContext: Context) {
        @JavascriptInterface
        fun showCamera() {
            val intent = Intent(this@MainActivity, CameraActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    }
}

