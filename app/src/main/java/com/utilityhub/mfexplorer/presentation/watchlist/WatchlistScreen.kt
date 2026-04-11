package com.utilityhub.mfexplorer.presentation.watchlist

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.utilityhub.mfexplorer.domain.model.WatchlistFolder
import com.utilityhub.mfexplorer.presentation.common.Screen
import com.utilityhub.mfexplorer.presentation.common.UiState
import com.utilityhub.mfexplorer.ui.theme.*


@Composable
fun WatchlistScreen(
    navController: NavController,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.darkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            WatchlistTopBar()

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> ShimmerList()

                    is UiState.Error -> {
                        Text(
                            text = state.message,
                            color = colors.negativeRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }

                    is UiState.Empty -> EmptyWatchlistState(
                        modifier = Modifier.align(Alignment.Center)
                    )

                    is UiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 20.dp,
                                vertical = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data, key = { it.id }) { folder ->
                                WatchlistFolderCard(
                                    folder = folder,
                                    accentColor = colors.folderAccents[
                                        folder.id.hashCode().and(0x7FFFFFFF) % colors.folderAccents.size
                                    ],
                                    onClick = {
                                        navController.navigate(
                                            Screen.WatchlistDetail.createRoute(folder.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistTopBar() {
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.darkBg)
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            colors.accentGold.copy(alpha = 0f),
                            colors.accentGold.copy(alpha = 0.35f),
                            colors.accentGold.copy(alpha = 0f)
                        )
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Watchlist",
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )
                Text(
                    text = "Your saved portfolios",
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accentGold.copy(alpha = 0.10f))
                    .border(1.dp, colors.accentGold.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = colors.accentGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WatchlistFolderCard(
    folder: WatchlistFolder,
    accentColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current

    val initials = folder.name
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
                    listOf(accentColor.copy(alpha = 0.22f), colors.dividerColor)
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (folder.fundCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FUNDS",
                            color = colors.textTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${folder.fundCount}",
                            color = accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Empty portfolio",
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyWatchlistState(modifier: Modifier = Modifier) {
    val colors = LocalCustomColors.current
    Column(
        modifier = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.accentGold.copy(alpha = 0.08f))
                .border(1.dp, colors.accentGold.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = colors.accentGold.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No portfolios yet",
            color = colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.3).sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Explore funds and save them into\ncustom portfolios to track here.",
            color = colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ShimmerList() {
    val colors = LocalCustomColors.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            tween(850, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false
    ) {
        items(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceCard.copy(alpha = alpha))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.dividerColor.copy(alpha = alpha))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f).height(13.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.dividerColor.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f).height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.dividerColor.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}