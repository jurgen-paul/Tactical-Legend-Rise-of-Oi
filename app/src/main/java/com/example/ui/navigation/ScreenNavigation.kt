package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object MainMenu : Screen("main_menu", "War Room", Icons.Default.Dashboard)
    object Campaign : Screen("campaign", "Campaign", Icons.Default.Map)
    object Squad : Screen("squad", "Oi Squad", Icons.Default.Groups)
    object Armory : Screen("armory", "Armory", Icons.Default.Shield)
    object Arcade : Screen("arcade", "Arcade", Icons.Default.SportsEsports)
    object VideoPlayer : Screen("video_player", "Cinema Stream", Icons.Default.OndemandVideo)
    object Store : Screen("store", "Cyber Store", Icons.Default.ShoppingCart)
    object Codex : Screen("codex", "Codex", Icons.Default.MenuBook)
    object Battle : Screen("battle/{missionId}", "Tactical Battle", Icons.Default.SportsEsports) {
        fun createRoute(missionId: Int) = "battle/$missionId"
    }
}
