package com.utilityhub.mfexplorer.presentation.explore

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.domain.model.FundCategory
import com.utilityhub.mfexplorer.presentation.common.Screen
import com.utilityhub.mfexplorer.ui.theme.*


@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val colors = LocalCustomColors.current
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.darkBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { ExploreTopBar(onViewAllClick = { navController.navigate(Screen.ViewAll.createRoute("ALL")) }) }
            item { ExploreSearchBar(onClick = { navController.navigate(Screen.Search.route) }) }

            state.error?.let { msg ->
                item {
                    val isNetworkError = msg.contains("Unable to resolve host", ignoreCase = true) || 
                                         msg.contains("No address associated", ignoreCase = true) ||
                                         msg.contains("Failed to connect", ignoreCase = true) ||
                                         msg.contains("timeout", ignoreCase = true)
                    
                    val isServerError = msg.contains("502", ignoreCase = true) ||
                                        msg.contains("503", ignoreCase = true) ||
                                        msg.contains("500", ignoreCase = true)

                    val displayMessage = if (isNetworkError) {
                        "No internet connection found. Please connect to the internet and try again."
                    } else if (isServerError) {
                        "Servers are down, please try again after some time."
                    } else {
                        msg
                    }

                    Text(
                        text = displayMessage,
                        color = colors.negativeRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            FundCategory.entries.forEach { category ->
                item {
                    CategorySection(
                        category = category,
                        funds = state.categoryFunds[category] ?: emptyList(),
                        isLoading = category in state.loadingCategories,
                        onViewAll = {
                            navController.navigate(Screen.ViewAll.createRoute(category.name))
                        },
                        onFundClick = { fund ->
                            navController.navigate(Screen.FundDetail.createRoute(fund.schemeCode))
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ExploreTopBar(onViewAllClick: () -> Unit) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "MF Explorer",
            color = colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.8).sp
        )

        // "View All >" pill — matches wireframe
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceCard)
                .border(1.dp, colors.dividerColor, RoundedCornerShape(10.dp))
                .clickable { onViewAllClick() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "View All",
                    color = colors.accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = ">",
                    color = colors.accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun ExploreSearchBar(onClick: () -> Unit) {
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceDark)
            .border(1.dp, colors.dividerColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = colors.textSecondary,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "Search funds...",
                color = colors.textTertiary,
                fontSize = 14.sp
            )
        }
    }
}


@Composable
private fun CategorySection(
    category: FundCategory,
    funds: List<Fund>,
    isLoading: Boolean,
    onViewAll: () -> Unit,
    onFundClick: (Fund) -> Unit
) {
    val colors = LocalCustomColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name.replace("_", " ").uppercase(),
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "View All  →",
                    color = colors.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .width(32.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(listOf(colors.accentGold, colors.accentGold.copy(alpha = 0f))),
                    RoundedCornerShape(1.dp)
                )
        )

        when {
            isLoading -> ShimmerRow()
            funds.isEmpty() -> EmptyCategory()
            else -> LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(funds, key = { it.schemeCode }) { fund ->
                    FundCard(
                        fund = fund,
                        accentColor = colors.cardAccents[
                            fund.schemeCode.hashCode().and(0x7FFFFFFF) % colors.cardAccents.size
                        ],
                        onClick = { onFundClick(fund) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FundCard(
    fund: Fund,
    accentColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    // 2-letter initials derived from the scheme name words
    val initials = fund.schemeName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceCard)
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.22f), colors.dividerColor)
                ),
                RoundedCornerShape(16.dp)
            )

            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = 0f),
                            accentColor.copy(alpha = 0.55f),
                            accentColor.copy(alpha = 0f)
                        )
                    ),
                    start = Offset(20f, 0f),
                    end = Offset(size.width - 20f, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.13f))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = initials,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = fund.schemeName,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "NAV",
                    color = colors.textTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
                Text(
                    text = if (fund.latestNav.isNotBlank()) "₹${String.format("%.4f", fund.latestNav.toDoubleOrNull() ?: 0.0)}" else "₹—",
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun ShimmerRow() {
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

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(3) {
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceCard.copy(alpha = alpha))
                    .padding(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp).height(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(colors.dividerColor.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.dividerColor.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f).height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.dividerColor.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .width(56.dp).height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.dividerColor.copy(alpha = alpha))
                )
            }
        }
    }
}


@Composable
private fun EmptyCategory() {
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No funds available", color = colors.textTertiary, fontSize = 13.sp)
    }
}