package com.komikita.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.komikita.app.domain.model.UserRole
import com.komikita.app.presentation.navigation.Screen

@Composable
fun BottomNavBar(
    userRole: UserRole,
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar {
        // 1. Home Tab
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        // 2. Favorit Tab
        NavigationBarItem(
            selected = currentRoute == Screen.Favorites.route,
            onClick = { onNavigate(Screen.Favorites) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorit") },
            label = { Text("Favorit") }
        )

        // 3. Riwayat Tab
        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = { onNavigate(Screen.History) },
            icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
            label = { Text("Riwayat") }
        )

        // 4. Role-Specific Middle Tab (Unduhan / Translator / Admin)
        when (userRole) {
            UserRole.ADMIN -> {
                NavigationBarItem(
                    selected = currentRoute == Screen.AdminDashboard.route,
                    onClick = { onNavigate(Screen.AdminDashboard) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                    label = { Text("Admin") }
                )
            }
            UserRole.TRANSLATOR -> {
                NavigationBarItem(
                    selected = currentRoute == Screen.TranslatorDashboard.route,
                    onClick = { onNavigate(Screen.TranslatorDashboard) },
                    icon = { Icon(Icons.Default.Translate, contentDescription = "Translator") },
                    label = { Text("Translator") }
                )
            }
            UserRole.USER, UserRole.GUEST -> {
                NavigationBarItem(
                    selected = currentRoute == Screen.Downloads.route,
                    onClick = { onNavigate(Screen.Downloads) },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Unduhan") },
                    label = { Text("Unduhan") }
                )
            }
        }

        // 5. Profil Tab
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") }
        )
    }
}
