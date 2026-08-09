package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BattleViewModel
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TacticalLegendTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(
    mainViewModel: MainViewModel = viewModel(),
    battleViewModel: BattleViewModel = viewModel()
) {
    val navController = rememberNavController()

    val profile by mainViewModel.playerProfile.collectAsStateWithLifecycle()
    val heroes by mainViewModel.heroes.collectAsStateWithLifecycle()
    val gearList by mainViewModel.gearList.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavScreens = listOf(
        Screen.MainMenu,
        Screen.Campaign,
        Screen.Squad,
        Screen.Armory,
        Screen.Arcade,
        Screen.Codex
    )

    val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CyberSurface,
                    contentColor = CyberPrimary,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) CyberPrimary else CyberSubtext
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) CyberPrimary else CyberSubtext,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = CyberSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        containerColor = CyberBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MainMenu.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.MainMenu.route) {
                MainMenuScreen(
                    profile = profile,
                    onNavigateToCampaign = { navController.navigate(Screen.Campaign.route) },
                    onNavigateToSquad = { navController.navigate(Screen.Squad.route) },
                    onNavigateToArmory = { navController.navigate(Screen.Armory.route) },
                    onNavigateToArcade = { navController.navigate(Screen.Arcade.route) },
                    onNavigateToCodex = { navController.navigate(Screen.Codex.route) },
                    onLaunchMission = { missionId ->
                        battleViewModel.startMission(missionId, heroes, gearList)
                        navController.navigate(Screen.Battle.createRoute(missionId))
                    }
                )
            }

            composable(Screen.Campaign.route) {
                CampaignScreen(
                    unlockedMissionId = profile?.unlockedMissionId ?: 1,
                    onLaunchMission = { missionId ->
                        battleViewModel.startMission(missionId, heroes, gearList)
                        navController.navigate(Screen.Battle.createRoute(missionId))
                    }
                )
            }

            composable(Screen.Squad.route) {
                SquadWarRoomScreen(
                    heroes = heroes,
                    gearList = gearList,
                    onToggleSquad = { heroId, inSquad ->
                        mainViewModel.toggleSquadStatus(heroId, inSquad)
                    }
                )
            }

            composable(Screen.Armory.route) {
                ArmoryScreen(
                    profile = profile,
                    gearList = gearList,
                    heroes = heroes,
                    onCraftGear = { onSuccess, onError ->
                        mainViewModel.craftGear(
                            onSuccess = { onSuccess() },
                            onError = { onError() }
                        )
                    },
                    onEquipItem = { heroId, gear ->
                        mainViewModel.equipItem(heroId, gear)
                    },
                    onUnequipItem = { gearId, heroId, gearType ->
                        mainViewModel.unequipItem(gearId, heroId, gearType)
                    }
                )
            }

            composable(Screen.Arcade.route) {
                ArcadeScreen(
                    profile = profile,
                    onClaimRewards = { credits, data ->
                        mainViewModel.claimArcadeRewards(credits, data)
                    }
                )
            }

            composable(Screen.Codex.route) {
                CodexScreen()
            }

            composable(
                route = Screen.Battle.route,
                arguments = listOf(navArgument("missionId") { type = NavType.IntType })
            ) {
                TacticalBattleScreen(
                    viewModel = battleViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
