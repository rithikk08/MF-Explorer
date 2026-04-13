package com.utilityhub.mfexplorer.presentation.search

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.presentation.common.Screen
import com.utilityhub.mfexplorer.presentation.common.UiState
import com.utilityhub.mfexplorer.ui.theme.*

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val colors = LocalCustomColors.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.darkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            SearchTopBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onBack = { navController.navigateUp() },
                focusRequester = focusRequester
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {

                    is UiState.Empty -> {
                        val message = when {
                            searchQuery.length >= 2 -> "No funds found for \"$searchQuery\""
                            searchQuery.isNotEmpty() -> "Type at least 2 characters..."
                            else -> null
                        }
                        message?.let {
                            Text(
                                text = it,
                                color = colors.textTertiary,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp)
                            )
                        }

                        if (searchQuery.isEmpty()) {
                            IdleHint(modifier = Modifier.align(Alignment.Center))
                        }
                    }

                    is UiState.Loading -> SearchShimmer()

                    is UiState.Error -> {
                        val isNetworkError = state.message.contains("Unable to resolve host", ignoreCase = true) || 
                                             state.message.contains("No address associated", ignoreCase = true) ||
                                             state.message.contains("Failed to connect", ignoreCase = true) ||
                                             state.message.contains("timeout", ignoreCase = true)
                        
                        val isServerError = state.message.contains("502", ignoreCase = true) ||
                                            state.message.contains("503", ignoreCase = true) ||
                                            state.message.contains("500", ignoreCase = true)

                        val displayMessage = if (isNetworkError) {
                            "No internet connection found. Please connect to the internet and try again."
                        } else if (isServerError) {
                            "Servers are down, please try again after some time."
                        } else {
                            state.message
                        }

                        Text(
                            text = displayMessage,
                            color = colors.negativeRed,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }

                    is UiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 20.dp,
                                vertical = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data, key = { it.schemeCode }) { fund ->
                                FundSearchItem(
                                    fund = fund,
                                    accentColor = colors.cardAccents[
                                        fund.schemeCode.hashCode()
                                            .and(0x7FFFFFFF) % colors.cardAccents.size
                                    ],
                                    onClick = {
                                        navController.navigate(
                                            Screen.FundDetail.createRoute(fund.schemeCode)
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
private fun SearchTopBar(

    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester
) {
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
                .padding(horizontal = 8.dp)
                .padding(top = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceDark)
                    .border(1.dp, colors.dividerColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search funds...",
                                color = colors.textTertiary,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(colors.accentGold),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                    }

                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onQueryChange("") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FundSearchItem(
    fund: Fund,
    accentColor: androidx.compose.ui.graphics.Color,
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

            Text(
                text = fund.schemeName,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "›",
                color = colors.textTertiary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun IdleHint(modifier: Modifier = Modifier) {
    val colors = LocalCustomColors.current
    Column(
        modifier = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colors.accentGold.copy(alpha = 0.08f))
                .border(1.dp, colors.accentGold.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.accentGold.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Search mutual funds",
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Type a fund category or AMC\nto find what you're looking for.",
            color = colors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SearchShimmer() {
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
        items(7) {
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
                            .fillMaxWidth(0.8f).height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.dividerColor.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f).height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.dividerColor.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}