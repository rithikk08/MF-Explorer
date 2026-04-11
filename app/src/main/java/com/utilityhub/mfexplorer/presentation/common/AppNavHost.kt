package com.utilityhub.mfexplorer.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.utilityhub.mfexplorer.presentation.explore.ExploreScreen
import com.utilityhub.mfexplorer.presentation.funddetail.FundDetailScreen
import com.utilityhub.mfexplorer.presentation.search.SearchScreen
import com.utilityhub.mfexplorer.presentation.viewall.ViewAllScreen
import com.utilityhub.mfexplorer.presentation.watchlist.WatchlistScreen
import com.utilityhub.mfexplorer.ui.theme.*

private val bottomBarRoutes = setOf(Screen.Explore.route, Screen.Watchlist.route)

@Composable
fun AppNavHost() {
    val colors = LocalCustomColors.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = colors.darkBg,
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onExploreClick = {
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onWatchlistClick = {
                        navController.navigate(Screen.Watchlist.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Explore.route) {
                ExploreScreen(navController = navController)
            }
            composable(Screen.Search.route) {
                SearchScreen(navController = navController)
            }
            composable(Screen.Watchlist.route) {
                WatchlistScreen(navController = navController)
            }
            composable(Screen.ViewAll.route) {
                ViewAllScreen(navController = navController)
            }
            composable(Screen.WatchlistDetail.route) {
                com.utilityhub.mfexplorer.presentation.watchlist.WatchlistDetailScreen(navController = navController)
            }
            composable(Screen.FundDetail.route) {
                FundDetailScreen(navController = navController)
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onExploreClick: () -> Unit,
    onWatchlistClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceDark)
            .navigationBarsPadding()
            // Gold top-edge glow line
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            colors.accentGold.copy(alpha = 0f),
                            colors.accentGold.copy(alpha = 0.4f),
                            colors.accentGold.copy(alpha = 0f)
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                label = "Explore",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                selected = currentRoute == Screen.Explore.route,
                onClick = onExploreClick
            )
            BottomBarItem(
                label = "Watchlist",
                selectedIcon = Icons.Filled.Star,
                unselectedIcon = Icons.Outlined.StarOutline,
                selected = currentRoute == Screen.Watchlist.route,
                onClick = onWatchlistClick
            )
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val iconTint  = if (selected) colors.accentGold else colors.textTertiary
    val labelColor = if (selected) colors.accentGold else colors.textTertiary
    val fontWeightValue = if (selected) FontWeight.Bold else FontWeight.Normal

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            // Top indicator pip when selected
            .drawBehind {
                if (selected) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                colors.accentGold.copy(alpha = 0f),
                                colors.accentGold,
                                colors.accentGold.copy(alpha = 0f)
                            )
                        ),
                        start = Offset(size.width * 0.25f, 0f),
                        end = Offset(size.width * 0.75f, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            .then(
                Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = fontWeightValue,
            letterSpacing = 0.3.sp
        )
    }
}