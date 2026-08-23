package com.komikita.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.komikita.app.domain.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "komikita_theme_settings")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val CUSTOM_AVATAR_URI_KEY = stringPreferencesKey("custom_avatar_uri")

    val appThemeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
        runCatching { AppTheme.valueOf(themeName) }.getOrDefault(AppTheme.SYSTEM)
    }

    val customAvatarUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_AVATAR_URI_KEY]
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setCustomAvatarUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_AVATAR_URI_KEY] = uri
        }
    }
}
