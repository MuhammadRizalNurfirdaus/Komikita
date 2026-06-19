package com.example.komikita.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom Navigation Bar untuk KOMIKITA.
 * Menyediakan navigasi cepat ke: Home, Favorit, Riwayat, Profil.
 *
 * Menggunakan Material 3 NavigationBar dengan ikon filled/outlined
 * yang berubah berdasarkan state terpilih.
 */
@Composable
fun KomikitaBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

/**
 * Item navigasi bottom bar.
 */
data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Daftar item bottom bar:
 * 0 = Home, 1 = Favorit, 2 = Riwayat, 3 = Profil
 */
val items = listOf(
    BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Favorit", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomNavItem("Riwayat", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
    BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person)
)
