package nz.co.chrisstevens.coparenting.feature.settings.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemePreference { SYSTEM, LIGHT, DARK }

/**
 * A single preference value, so plain SharedPreferences is simpler and more appropriate here
 * than the JSON-file-per-list pattern used by the other repositories.
 */
class ThemePreferenceRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var currentTheme: ThemePreference by mutableStateOf(readTheme())

    val theme: ThemePreference get() = currentTheme

    fun setTheme(preference: ThemePreference) {
        currentTheme = preference
        prefs.edit().putString(KEY_THEME, preference.name).apply()
    }

    private fun readTheme(): ThemePreference =
        runCatching { ThemePreference.valueOf(prefs.getString(KEY_THEME, null) ?: "") }
            .getOrDefault(ThemePreference.SYSTEM)

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_THEME = "theme_preference"
    }
}
