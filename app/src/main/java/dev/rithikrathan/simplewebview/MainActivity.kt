package dev.rithikrathan.simplewebview

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.rithikrathan.simplewebview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openButton.setOnClickListener { openUrl() }
        binding.urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                openUrl()
                true
            } else {
                false
            }
        }
    }

    private fun openUrl() {
        val url = normalizeUrl(binding.urlInput.text.toString().trim())
        if (url == null) {
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(
            Intent(this, WebViewActivity::class.java).putExtra(WebViewActivity.EXTRA_URL, url)
        )
        finish()
    }

    private fun normalizeUrl(raw: String): String? {
        if (raw.isEmpty()) return null
        val withScheme = if (raw.contains("://")) raw else "https://$raw"
        return withScheme.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    }
}
