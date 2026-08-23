package com.komikita.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komikita.app.data.local.datastore.ThemePreferences
import com.komikita.app.domain.model.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = themePreferences.appThemeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppTheme.SYSTEM
    )

    val customAvatarUri: StateFlow<String?> = themePreferences.customAvatarUriFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            themePreferences.setAppTheme(theme)
        }
    }

    fun setCustomAvatarUri(uri: String) {
        viewModelScope.launch {
            themePreferences.setCustomAvatarUri(uri)
        }
    }
}
