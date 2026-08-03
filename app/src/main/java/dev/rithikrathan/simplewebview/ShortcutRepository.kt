package dev.rithikrathan.simplewebview

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Shortcut(val name: String, val url: String)

object ShortcutRepository {
    private const val PREFS_NAME = "shortcuts"
    private const val KEY_LIST = "list"

    fun load(context: Context): List<Shortcut> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LIST, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(Shortcut(obj.getString("name"), obj.getString("url")))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, shortcuts: List<Shortcut>) {
        val array = JSONArray()
        shortcuts.forEach { shortcut ->
            array.put(
                JSONObject()
                    .put("name", shortcut.name)
                    .put("url", shortcut.url)
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LIST, array.toString())
            .apply()
    }
}
