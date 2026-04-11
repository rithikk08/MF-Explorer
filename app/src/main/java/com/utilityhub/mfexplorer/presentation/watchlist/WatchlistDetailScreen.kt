package com.utilityhub.mfexplorer.presentation.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.utilityhub.mfexplorer.domain.model.WatchlistFund
import com.utilityhub.mfexplorer.presentation.common.Screen
import com.utilityhub.mfexplorer.presentation.common.UiState
import com.utilityhub.mfexplorer.ui.theme.*
import androidx.compose.foundation.BorderStroke
@Composable
fun WatchlistDetailScreen(
    navController: NavController,
    viewModel: WatchlistDetailViewModel = hiltViewModel()
) {
    val folderName by viewModel.folderName.collectAsState()
    val fundsState by viewModel.funds.collectAsState()
    val colors = LocalCustomColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.darkBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.darkBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(top = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = folderName,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = fundsState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.accentGold
                    )
                }
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        color = colors.negativeRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Empty -> {
                    EmptyFolderState(
                        modifier = Modifier.align(Alignment.Center),
                        onExploreClick = {
                            navController.popBackStack(Screen.Watchlist.route, inclusive = false)
                            navController.navigate(Screen.Explore.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data, key = { it.schemeCode }) { fund ->
                            WatchlistFundItem(
                                fund = fund,
                                accentColor = colors.cardAccents[fund.schemeCode.hashCode().and(0x7FFFFFFF) % colors.cardAccents.size],
                                onClick = {
                                    navController.navigate(Screen.FundDetail.createRoute(fund.schemeCode))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistFundItem(
    fund: WatchlistFund,
    accentColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val initials = fund.schemeName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceCard)
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.2f), colors.dividerColor)
                ),
                RoundedCornerShape(14.dp)
            )
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = 0f),
                            accentColor.copy(alpha = 0.45f),
                            accentColor.copy(alpha = 0f)
                        )
                    ),
                    start = Offset(24f, 0f),
                    end = Offset(size.width - 24f, 0f),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            // central details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fund.schemeName,
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NAV",
                        color = colors.textTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (fund.latestNav.isNotBlank()) "₹${String.format("%.4f", fund.latestNav.toDoubleOrNull() ?: 0.0)}" else "₹—",
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View",
                tint = colors.textTertiary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun EmptyFolderState(
    modifier: Modifier = Modifier,
    onExploreClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.accentGold.copy(alpha = 0.08f))
                .border(1.dp, colors.accentGold.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = colors.accentGold.copy(alpha = 0.7f),
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No funds added yet.",
            color = colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Explore the market to save funds into this portfolio.",
            color = colors.textTertiary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 36.dp)
        )

        OutlinedButton(
            onClick = onExploreClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.textPrimary
            ),
            border = border(1.dp, colors.textTertiary, RoundedCornerShape(12.dp)),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Explore Funds",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape) = BorderStroke(width, color)
