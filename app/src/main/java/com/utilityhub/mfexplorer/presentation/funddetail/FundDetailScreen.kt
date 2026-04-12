package com.utilityhub.mfexplorer.presentation.funddetail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.domain.model.NavPoint
import com.utilityhub.mfexplorer.domain.model.WatchlistFolder
import com.utilityhub.mfexplorer.presentation.common.UiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.utilityhub.mfexplorer.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundDetailScreen(
    navController: NavController,
    viewModel: FundDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()
    val selectedFolderIds by viewModel.selectedFolderIds.collectAsState()
    val chartUiState by viewModel.chartUiState.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    val colors = LocalCustomColors.current
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.darkBg)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { },
                    modifier = Modifier.padding(top = 6.dp),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                    },
                    actions = {
                        if (uiState is UiState.Success) {
                            IconButton(onClick = { showBottomSheet = true }) {
                                AnimatedContent(
                                    targetState = isInWatchlist,
                                    transitionSpec = {
                                        scaleIn(tween(200)) + fadeIn() togetherWith
                                                scaleOut(tween(200)) + fadeOut()
                                    },
                                    label = "watchlist_icon"
                                ) { inWatchlist ->
                                    Icon(
                                        imageVector = if (inWatchlist) Icons.Default.Bookmark
                                        else Icons.Default.BookmarkBorder,
                                        contentDescription = "Watchlist",
                                        tint = if (inWatchlist) colors.accentGold else colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = colors.surfaceDark
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = colors.accentGold, strokeWidth = 2.dp)
                            Text("Loading fund data…", color = colors.textSecondary, fontSize = 13.sp)
                        }
                    }

                    is UiState.Error -> {
                        val isNetworkError = state.message.contains("Unable to resolve host", ignoreCase = true) || 
                                             state.message.contains("No address associated", ignoreCase = true) ||
                                             state.message.contains("Failed to connect", ignoreCase = true) ||
                                             state.message.contains("timeout", ignoreCase = true)
                        
                        val displayMessage = if (isNetworkError) {
                            "No internet connection found. Please connect to the internet and try again."
                        } else {
                            state.message
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠", fontSize = 32.sp)
                            Text(
                                text = displayMessage,
                                color = colors.negativeRed,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    is UiState.Empty -> {
                        Text(
                            text = "Fund not found.",
                            color = colors.textSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is UiState.Success -> {
                        val detail = state.data
                        val basicFund = Fund(
                            schemeCode      = detail.schemeCode,
                            schemeName      = detail.schemeName,
                            fundHouse       = detail.fundHouse,
                            schemeType      = detail.schemeType,
                            schemeCategory  = detail.schemeCategory,
                            latestNav       = detail.latestNav
                        )

                        val rangeReturn = chartUiState?.rangeReturn
                        val filteredHistory = chartUiState?.filteredHistory ?: emptyList()
                        val chartEntries = chartUiState?.entries ?: emptyList()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            FundHeroSection(
                                schemeName = detail.schemeName,
                                fundHouse = detail.fundHouse,
                                schemeType = detail.schemeType,
                                schemeCategory = detail.schemeCategory,
                                latestNav = detail.latestNav,
                                navChange = detail.navChange,
                                rangeReturn = rangeReturn,
                                selectedRange = selectedRange
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ChartCard(
                                navHistory    = filteredHistory,
                                chartEntries  = chartEntries,
                                selectedRange = selectedRange,
                                isPositive    = chartUiState?.isPositive ?: true,
                                onRangeSelected = { viewModel.setTimeRange(it) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            FundInfoRow(
                                schemeType = detail.schemeType,
                                fundHouse  = detail.fundHouse,
                                schemeCategory = detail.schemeCategory
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        if (showBottomSheet) {
                            AddToWatchlistBottomSheet(
                                folders           = allFolders,
                                selectedFolderIds = selectedFolderIds,
                                onFolderToggle    = { folderId ->
                                    viewModel.toggleFolderSelection(folderId, basicFund)
                                },
                                onCreateNew = { folderName ->
                                    viewModel.createFolderAndAddFund(folderName, basicFund)
                                },
                                onDismiss = { showBottomSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FundHeroSection(
    schemeName: String,
    fundHouse: String,
    schemeType: String,
    schemeCategory: String,
    latestNav: String,
    navChange: Double,
    rangeReturn: Double?,
    selectedRange: TimeRange
) {
    val colors = LocalCustomColors.current
    val isPositiveDay   = navChange >= 0
    val isPositiveRange = (rangeReturn ?: 0.0) >= 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.darkBg)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Surface(
                shape  = RoundedCornerShape(4.dp),
                color  = colors.accentGoldDim.copy(alpha = 0.25f),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(
                    text     = fundHouse.uppercase(),
                    color    = colors.accentGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Text(
                text       = schemeName,
                color      = colors.textPrimary,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                maxLines   = 3,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoPill(text = schemeCategory)
                InfoPill(text = schemeType)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment    = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier             = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text     = "NET ASSET VALUE",
                        color    = colors.textTertiary,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text       = "₹ ${String.format("%.4f", latestNav.toDoubleOrNull() ?: 0.0)}",
                        color      = colors.textPrimary,
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ChangeBadge(
                        label    = "Today",
                        value    = navChange,
                        positive = isPositiveDay
                    )
                }

                if (rangeReturn != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text     = "${selectedRange.label} RETURN",
                            color    = colors.textTertiary,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ChangeBadge(
                            label    = selectedRange.label,
                            value    = rangeReturn,
                            positive = isPositiveRange,
                            large    = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(text: String) {
    val colors = LocalCustomColors.current
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = colors.dividerColor
    ) {
        Text(
            text     = text,
            color    = colors.textSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ChangeBadge(
    label: String,
    value: Double,
    positive: Boolean,
    large: Boolean = false
) {
    val colors = LocalCustomColors.current
    val color  = if (positive) colors.positiveGreen else colors.negativeRed
    val arrow  = if (positive) "▲" else "▼"
    val prefix = if (positive) "+" else ""

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text       = "$arrow $prefix${String.format("%.2f", value)}%",
            color      = color,
            fontSize   = if (large) 16.sp else 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ChartCard(
    navHistory: List<NavPoint>,
    chartEntries: List<FloatEntry>,
    selectedRange: TimeRange,
    isPositive: Boolean,
    onRangeSelected: (TimeRange) -> Unit
) {
    val colors = LocalCustomColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            TimeRangeSelector(
                selected   = selectedRange,
                onSelected = onRangeSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (navHistory.isNotEmpty() && chartEntries.isNotEmpty()) {
                NavHistoryChart(navHistory = navHistory, entries = chartEntries, isPositive = isPositive)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("—", color = colors.textTertiary, fontSize = 24.sp)
                        Text("No data for this period", color = colors.textTertiary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selected: TimeRange,
    onSelected: (TimeRange) -> Unit
) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) colors.accentGold.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) colors.accentGold.copy(alpha = 0.5f) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelected(range) }
                    .padding(vertical = 7.dp)
            ) {
                Text(
                    text       = range.label,
                    color      = if (isSelected) colors.accentGold else colors.textTertiary,
                    fontSize   = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NavHistoryChart(
    navHistory: List<NavPoint>,
    entries: List<FloatEntry>,
    isPositive: Boolean
) {
    if (entries.isEmpty()) return

    val chartEntryModel = entryModelOf(entries)
    val colors = LocalCustomColors.current
    val lineColor  = if (isPositive) colors.positiveGreen else colors.negativeRed
    val gradTop    = if (isPositive) colors.positiveGreen.copy(alpha = 0.3f) else colors.negativeRed.copy(alpha = 0.3f)
    val gradBot    = Color.Transparent

    Chart(
        chart = lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = lineColor,
                    lineThickness = 2.dp,
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        Brush.verticalGradient(listOf(gradTop, gradBot))
                    )
                )
            )
        ),
        model       = chartEntryModel,
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        startAxis   = rememberStartAxis(
            label = rememberAxisLabelComponent(color = colors.textTertiary, textSizeSp = 9f),
            itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)
        ),
        bottomAxis  = rememberBottomAxis(
            label     = rememberAxisLabelComponent(color = colors.textTertiary, textSizeSp = 9f),
            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 1, shiftExtremeTicks = false),
            valueFormatter = { value, _ ->
                val idx = value.toInt().coerceIn(0, navHistory.size - 1)
                try {
                    val date = LocalDate.parse(navHistory[idx].date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    if (navHistory.size <= 10) {
                        date.format(DateTimeFormatter.ofPattern("d MMM"))
                    } else {
                        date.format(DateTimeFormatter.ofPattern("MMM yy"))
                    }
                } catch (e: Exception) { "" }
            },
            guideline = null
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}

@Composable
private fun rememberAxisLabelComponent(
    color: Color,
    textSizeSp: Float
) = com.patrykandpatrick.vico.compose.component.textComponent(
    color    = color,
    textSize = textSizeSp.sp
)

@Composable
private fun FundInfoRow(
    schemeType: String,
    fundHouse: String,
    schemeCategory: String
) {
    val colors = LocalCustomColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCell(label = "TYPE",     value = schemeType)
            VerticalDivider()
            InfoCell(label = "CATEGORY", value = schemeCategory, maxLines = 2)
            VerticalDivider()
            InfoCell(label = "AMC",      value = fundHouse, maxLines = 2)
        }
    }
}

@Composable
private fun InfoCell(label: String, value: String, maxLines: Int = 1) {
    val colors = LocalCustomColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(max = 100.dp)
    ) {
        Text(
            text          = label,
            color         = colors.textTertiary,
            fontSize      = 9.sp,
            letterSpacing = 1.sp,
            fontWeight    = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text       = value,
            color      = colors.textPrimary,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines   = maxLines,
            overflow   = TextOverflow.Ellipsis,
            lineHeight  = 16.sp
        )
    }
}

@Composable
private fun VerticalDivider() {
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(colors.dividerColor)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToWatchlistBottomSheet(
    folders: List<WatchlistFolder>,
    selectedFolderIds: List<Long>,
    onFolderToggle: (Long) -> Unit,
    onCreateNew: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCustomColors.current
    var newFolderName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = colors.surfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(colors.textTertiary, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                "Save to Portfolio",
                color      = colors.textPrimary,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Organise this fund into a portfolio",
                color    = colors.textSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder   = { Text("New portfolio name", color = colors.textTertiary, fontSize = 13.sp) },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colors.accentGold,
                        unfocusedBorderColor = colors.dividerColor,
                        focusedTextColor     = colors.textPrimary,
                        unfocusedTextColor   = colors.textPrimary,
                        cursorColor          = colors.accentGold
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateNew(newFolderName.trim())
                            newFolderName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentGold,
                        contentColor   = Color(0xFF0D0F14)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = colors.dividerColor)
            Spacer(modifier = Modifier.height(8.dp))

            if (folders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No portfolios yet. Create one above.", color = colors.textTertiary, fontSize = 13.sp)
                }
            } else {
                folders.forEach { folder ->
                    val isChecked = selectedFolderIds.contains(folder.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onFolderToggle(folder.id) }
                            .background(
                                if (isChecked) colors.accentGold.copy(alpha = 0.07f)
                                else Color.Transparent
                            )
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = isChecked,
                            onCheckedChange  = { onFolderToggle(folder.id) },
                            colors           = CheckboxDefaults.colors(
                                checkedColor         = colors.accentGold,
                                uncheckedColor       = colors.textTertiary,
                                checkmarkColor       = Color(0xFF0D0F14)
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text       = folder.name,
                            color      = if (isChecked) colors.textPrimary else colors.textSecondary,
                            fontSize   = 15.sp,
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
