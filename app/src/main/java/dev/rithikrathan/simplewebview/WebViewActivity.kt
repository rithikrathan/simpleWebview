package dev.rithikrathan.simplewebview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.rithikrathan.simplewebview.databinding.ActivityWebviewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewBinding
    private var currentUrl: String? = null
    private var downX = 0f
    private var downY = 0f
    private var mainFrameLoaded = false
    private var hideBarRunnable: Runnable? = null

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterFullscreen()
        requestNotificationPermissionIfNeeded()
        setupWebView()
        setupEdgeSwipe()

        val url = intent.getStringExtra(EXTRA_URL)
        if (url.isNullOrEmpty()) {
            finish()
            return
        }
        currentUrl = url
        clearSessionData()
        binding.webview.loadUrl(url)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webview.canGoBack()) {
                        binding.webview.goBack()
                    } else {
                        returnHome()
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        hideBarRunnable?.let { binding.loadingBar.removeCallbacks(it) }
        super.onDestroy()
    }

    override fun finish() {
        setResult(RESULT_OK, Intent().putExtra(EXTRA_URL, currentUrl ?: ""))
        super.finish()
    }

    private fun returnHome() {
        finish()
    }

    private fun clearSessionData() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        binding.webview.clearCache(true)
        binding.webview.clearHistory()
        binding.webview.clearFormData()
    }

    private fun setupWebView() {
        val webView = binding.webview
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(false)
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.setSaveFormData(false)
        @Suppress("DEPRECATION")
        settings.setSavePassword(false)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                mainFrameLoaded = false
                binding.loadingBar.isIndeterminate = false
                binding.loadingBar.progress = 0
                showLoadingBar()
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                if (mainFrameLoaded) {
                    binding.loadingBar.isIndeterminate = true
                    showLoadingBar()
                    hideLoadingBar(resetProgress = true, delayMillis = 600)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!url.isNullOrEmpty()) {
                    currentUrl = url
                }
                mainFrameLoaded = true
                completeLoadingBar()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress in 1..99) {
                    binding.loadingBar.isIndeterminate = false
                    binding.loadingBar.progress = newProgress
                    showLoadingBar()
                } else if (newProgress >= 100) {
                    completeLoadingBar()
                }
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest) {
                super.onPermissionRequestCanceled(request)
            }
        }
    }

    private fun showLoadingBar() {
        hideBarRunnable?.let { binding.loadingBar.removeCallbacks(it) }
        binding.loadingBar.animate().cancel()
        binding.loadingBar.visibility = View.VISIBLE
        binding.loadingBar.alpha = 1f
    }

    private fun completeLoadingBar() {
        binding.loadingBar.isIndeterminate = false
        binding.loadingBar.progress = 100
        showLoadingBar()
        hideLoadingBar(resetProgress = true, delayMillis = 300)
    }

    private fun hideLoadingBar(resetProgress: Boolean, delayMillis: Long) {
        hideBarRunnable?.let { binding.loadingBar.removeCallbacks(it) }
        val runnable = Runnable {
            binding.loadingBar.animate().alpha(0f).setDuration(250).withEndAction {
                binding.loadingBar.visibility = View.GONE
                binding.loadingBar.alpha = 1f
                if (resetProgress) {
                    binding.loadingBar.isIndeterminate = false
                    binding.loadingBar.progress = 0
                }
            }.start()
        }
        hideBarRunnable = runnable
        binding.loadingBar.postDelayed(runnable, delayMillis)
    }

    private fun setupEdgeSwipe() {
        val edge = resources.displayMetrics.density * 64f
        binding.webview.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                }

                MotionEvent.ACTION_UP -> {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (downX < edge && dx > edge * 1.5f && dx > Math.abs(dy) * 1.5f) {
                        returnHome()
                    }
                }
            }
            false
        }
    }

    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }
}
