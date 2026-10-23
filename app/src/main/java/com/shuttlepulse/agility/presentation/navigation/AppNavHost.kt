package com.shuttlepulse.agility.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.BuildCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shuttlepulse.agility.core.ui.theme.ShuttlePulseColors
import com.shuttlepulse.agility.di.AppModule
import com.shuttlepulse.agility.presentation.builder.BuilderViewModel
import com.shuttlepulse.agility.presentation.builder.CircuitBuilderScreen
import com.shuttlepulse.agility.presentation.diagnostics.DiagnosticsScreen
import com.shuttlepulse.agility.presentation.diagnostics.DiagnosticsViewModel
import com.shuttlepulse.agility.presentation.home.HomeScreen
import com.shuttlepulse.agility.presentation.home.HomeViewModel
import com.shuttlepulse.agility.presentation.library.DrillLibraryScreen
import com.shuttlepulse.agility.presentation.library.LibraryViewModel
import com.shuttlepulse.agility.presentation.player.PlayerViewModel
import com.shuttlepulse.agility.presentation.player.WorkoutPlayerScreen
import kotlinx.serialization.Serializable

sealed interface ShuttlePulseRoute {
    @Serializable
    data object Home : ShuttlePulseRoute

    @Serializable
    data object Library : ShuttlePulseRoute

    @Serializable
    data object Builder : ShuttlePulseRoute

    @Serializable
    data object Player : ShuttlePulseRoute

    @Serializable
    data object Diagnostics : ShuttlePulseRoute
}

private data class NavTabItem<T : Any>(
    val route: T,
    val label: String,
    val testTag: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val workoutRepository = AppModule.provideWorkoutRepository(context)
    val cadenceSoundEngine = AppModule.provideCadenceSoundEngine(context)
    val workoutTimer = AppModule.provideMicroPhaseWorkoutTimer()
    val generateWorkoutTimelineUseCase = AppModule.provideGenerateWorkoutTimelineUseCase()
    val auditFeatureHealthUseCase = AppModule.provideAuditFeatureHealthUseCase()

    val navItems = listOf(
        NavTabItem(
            route = ShuttlePulseRoute.Home,
            label = "Sessions",
            testTag = "nav_tab_home",
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard
        ),
        NavTabItem(
            route = ShuttlePulseRoute.Library,
            label = "Drills",
            testTag = "nav_tab_library",
            selectedIcon = Icons.Filled.MenuBook,
            unselectedIcon = Icons.Outlined.MenuBook
        ),
        NavTabItem(
            route = ShuttlePulseRoute.Builder,
            label = "Builder",
            testTag = "nav_tab_builder",
            selectedIcon = Icons.Filled.FitnessCenter,
            unselectedIcon = Icons.Outlined.FitnessCenter
        ),
        NavTabItem(
            route = ShuttlePulseRoute.Player,
            label = "Player",
            testTag = "nav_tab_player",
            selectedIcon = Icons.Filled.PlayCircleFilled,
            unselectedIcon = Icons.Outlined.PlayCircleOutline
        ),
        NavTabItem(
            route = ShuttlePulseRoute.Diagnostics,
            label = "Self-Test",
            testTag = "nav_tab_diagnostics",
            selectedIcon = Icons.Filled.BuildCircle,
            unselectedIcon = Icons.Outlined.BuildCircle
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ShuttlePulseColors.OledGymBlack,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = ShuttlePulseColors.GymSurfaceDark,
                windowInsets = WindowInsets.navigationBars
            ) {
                navItems.forEach { item ->
                    val isSelected = currentDestination?.hasRoute(item.route::class) == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.testTag(item.testTag),
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ShuttlePulseColors.OledGymBlack,
                            selectedTextColor = ShuttlePulseColors.VoltPlyoLime,
                            indicatorColor = ShuttlePulseColors.VoltPlyoLime,
                            unselectedIconColor = ShuttlePulseColors.TextSlateMuted,
                            unselectedTextColor = ShuttlePulseColors.TextSlateMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ShuttlePulseRoute.Home,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable<ShuttlePulseRoute.Home> {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(
                        workoutRepository = workoutRepository,
                        cadenceSoundEngine = cadenceSoundEngine
                    )
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onLaunchPlayer = {
                        navController.navigate(ShuttlePulseRoute.Player) {
                            launchSingleTop = true
                        }
                    },
                    onOpenBuilder = {
                        navController.navigate(ShuttlePulseRoute.Builder) {
                            launchSingleTop = true
                        }
                    },
                    onOpenLibrary = {
                        navController.navigate(ShuttlePulseRoute.Library) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<ShuttlePulseRoute.Library> {
                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.provideFactory(workoutRepository)
                )
                DrillLibraryScreen(
                    viewModel = libraryViewModel,
                    onLaunchQuickDrill = {
                        navController.navigate(ShuttlePulseRoute.Player) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<ShuttlePulseRoute.Builder> {
                val builderViewModel: BuilderViewModel = viewModel(
                    factory = BuilderViewModel.provideFactory(workoutRepository)
                )
                CircuitBuilderScreen(
                    viewModel = builderViewModel,
                    onLaunchBuiltCircuit = {
                        navController.navigate(ShuttlePulseRoute.Player) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<ShuttlePulseRoute.Player> {
                val playerViewModel: PlayerViewModel = viewModel(
                    factory = PlayerViewModel.provideFactory(
                        workoutRepository = workoutRepository,
                        cadenceSoundEngine = cadenceSoundEngine,
                        workoutTimer = workoutTimer,
                        generateWorkoutTimelineUseCase = generateWorkoutTimelineUseCase
                    )
                )
                WorkoutPlayerScreen(
                    viewModel = playerViewModel,
                    onExitPlayer = {
                        navController.navigate(ShuttlePulseRoute.Home) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<ShuttlePulseRoute.Diagnostics> {
                val diagnosticsViewModel: DiagnosticsViewModel = viewModel(
                    factory = DiagnosticsViewModel.provideFactory(
                        workoutRepository = workoutRepository,
                        cadenceSoundEngine = cadenceSoundEngine,
                        workoutTimer = workoutTimer,
                        auditFeatureHealthUseCase = auditFeatureHealthUseCase
                    )
                )
                DiagnosticsScreen(viewModel = diagnosticsViewModel)
            }
        }
    }
}
