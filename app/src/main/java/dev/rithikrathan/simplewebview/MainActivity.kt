package dev.rithikrathan.simplewebview

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.rithikrathan.simplewebview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ShortcutAdapter

    private val openLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val url = result.data?.getStringExtra(WebViewActivity.EXTRA_URL)
            if (!url.isNullOrEmpty()) {
                binding.urlInput.setText(url)
                binding.urlInput.setSelection(url.length)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge()
        clearBrowsingData()

        adapter = ShortcutAdapter(
            onOpen = { openUrl(it.url) },
            onDelete = { confirmDelete(it) }
        )
        binding.shortcutsGrid.layoutManager = GridLayoutManager(this, 3)
        binding.shortcutsGrid.adapter = adapter
        reloadShortcuts()

        binding.openButton.setOnClickListener { openFromBar() }
        binding.saveButton.setOnClickListener { saveFromBar() }
        binding.urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                openFromBar()
                true
            } else {
                false
            }
        }
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun clearBrowsingData() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
    }

    private fun openFromBar() {
        val url = normalizeUrl(binding.urlInput.text.toString())
        if (url == null) {
            toast(getString(R.string.invalid_url))
            return
        }
        openUrl(url)
    }

    private fun openUrl(url: String) {
        binding.urlInput.setText(url)
        binding.urlInput.setSelection(url.length)
        openLauncher.launch(
            Intent(this, WebViewActivity::class.java).putExtra(WebViewActivity.EXTRA_URL, url)
        )
    }

    private fun saveFromBar() {
        val url = normalizeUrl(binding.urlInput.text.toString())
        if (url == null) {
            toast(getString(R.string.invalid_url))
            return
        }
        val shortcuts = ShortcutRepository.load(this).toMutableList()
        if (shortcuts.none { it.url == url }) {
            shortcuts.add(Shortcut(hostOf(url)?.removePrefix("www.") ?: url, url))
            ShortcutRepository.save(this, shortcuts)
            reloadShortcuts()
            toast(getString(R.string.saved))
        }
    }

    private fun confirmDelete(shortcut: Shortcut) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_shortcut_title)
            .setMessage(getString(R.string.delete_shortcut_message, shortcut.name))
            .setPositiveButton(R.string.delete) { _, _ -> deleteShortcut(shortcut) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteShortcut(shortcut: Shortcut) {
        val shortcuts = ShortcutRepository.load(this).toMutableList()
        shortcuts.removeAll { it.url == shortcut.url }
        ShortcutRepository.save(this, shortcuts)
        reloadShortcuts()
    }

    private fun reloadShortcuts() {
        val shortcuts = ShortcutRepository.load(this)
        adapter.submitList(shortcuts)
        binding.emptyState.visibility = if (shortcuts.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
