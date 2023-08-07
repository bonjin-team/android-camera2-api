package kr.co.bonjin

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Toast
import kr.co.bonjin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

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
    }
}

class WebViewJavascriptInterface(private val mContext: Context) {
    @JavascriptInterface
    fun showCamera() {
        Toast.makeText(mContext, "showCamera", Toast.LENGTH_SHORT).show()
    }
}