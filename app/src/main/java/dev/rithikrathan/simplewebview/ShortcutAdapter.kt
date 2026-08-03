package dev.rithikrathan.simplewebview

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import dev.rithikrathan.simplewebview.databinding.ItemShortcutBinding

class ShortcutAdapter(
    private val onOpen: (Shortcut) -> Unit,
    private val onDelete: (Shortcut) -> Unit
) : RecyclerView.Adapter<ShortcutAdapter.ShortcutViewHolder>() {

    private val items = mutableListOf<Shortcut>()

    fun submitList(list: List<Shortcut>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
        val binding = ItemShortcutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShortcutViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ShortcutViewHolder(
        private val binding: ItemShortcutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shortcut: Shortcut) {
            val name = shortcut.name.ifBlank { hostOf(shortcut.url) ?: shortcut.url }
            binding.name.text = name
            binding.letterTile.text = name.firstOrNull()?.uppercase() ?: "?"
            binding.letterTile.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(LETTER_COLORS[(name.hashCode() and Int.MAX_VALUE) % LETTER_COLORS.size])
            }
            binding.letterTile.visibility = View.VISIBLE
            binding.favicon.visibility = View.GONE

            val domain = hostOf(shortcut.url)
            if (domain != null) {
                binding.favicon.load("https://www.google.com/s2/favicons?domain=$domain&sz=64") {
                    crossfade(true)
                    listener(
                        onSuccess = { _, _ ->
                            binding.favicon.visibility = View.VISIBLE
                            binding.letterTile.visibility = View.GONE
                        },
                        onError = { _, _ ->
                            binding.favicon.visibility = View.GONE
                            binding.letterTile.visibility = View.VISIBLE
                        }
                    )
                }
            }

            binding.root.setOnClickListener { onOpen(shortcut) }
            binding.root.setOnLongClickListener {
                onDelete(shortcut)
                true
            }
        }
    }

    companion object {
        private val LETTER_COLORS = intArrayOf(
            0xFF1565C0.toInt(), 0xFF00897B.toInt(), 0xFFF4511E.toInt(),
            0xFF7B1FA2.toInt(), 0xFFC2185B.toInt(), 0xFF558B2F.toInt(),
            0xFF00695C.toInt(), 0xFF5D4037.toInt(), 0xFF303F9F.toInt(),
            0xFFE64A19.toInt()
        )
    }
}
