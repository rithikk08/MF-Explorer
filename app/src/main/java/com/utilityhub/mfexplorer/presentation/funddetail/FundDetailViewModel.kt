package com.utilityhub.mfexplorer.presentation.funddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.domain.model.FundDetail
import com.utilityhub.mfexplorer.domain.repository.MfRepository
import com.utilityhub.mfexplorer.domain.repository.WatchlistRepository
import com.utilityhub.mfexplorer.presentation.common.UiState
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.utilityhub.mfexplorer.domain.model.NavPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class TimeRange(val label: String, val days: Int?) {
    WEEK_1("1W", 7),
    MONTH_1("1M", 30),
    MONTH_3("3M", 90),
    MONTH_6("6M", 180),
    YEAR_1("1Y", 365),
    YEAR_3("3Y", 1095),
    INCEPTION("All", null)
}

data class ChartUiState(
    val filteredHistory: List<com.utilityhub.mfexplorer.domain.model.NavPoint>,
    val rangeReturn: Double?,
    val isPositive: Boolean,
    val entries: List<FloatEntry>
)

@HiltViewModel
class FundDetailViewModel @Inject constructor(
    private val mfRepository: MfRepository,
    private val watchlistRepository: WatchlistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val schemeCodeStr: String = checkNotNull(savedStateHandle["schemeCode"])
    private val schemeCode = schemeCodeStr.toIntOrNull() ?: 0

    private val _uiState = MutableStateFlow<UiState<FundDetail>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    val isInWatchlist: StateFlow<Boolean> = watchlistRepository.isFundInAnyWatchlist(schemeCode)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val allFolders = watchlistRepository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedFolderIds = watchlistRepository.getFolderIdsForFund(schemeCode)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedRange = MutableStateFlow(TimeRange.YEAR_1)
    val selectedRange = _selectedRange.asStateFlow()

    val chartUiState: StateFlow<ChartUiState?> = combine(_uiState, _selectedRange) { state, range ->
        if (state is UiState.Success) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                val history = state.data.navHistory
                val filtered = filterNavHistory(history, range)
                
                val rangeReturn = if (filtered.size >= 2) {
                    val first = filtered.first().nav
                    val last  = filtered.last().nav
                    ((last - first) / first) * 100.0
                } else null

                val isPositive = if (filtered.isNotEmpty()) {
                    filtered.last().nav >= filtered.first().nav
                } else true

                val sampledData = downsampleData(filtered, range)
                val entries = sampledData.mapIndexed { i, point ->
                    FloatEntry(i.toFloat(), point.nav.toFloat())
                }

                ChartUiState(
                    filteredHistory = sampledData,
                    rangeReturn = rangeReturn,
                    isPositive = isPositive,
                    entries = entries
                )
            }
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        loadFundDetail()
    }

    private fun downsampleData(data: List<NavPoint>, range: TimeRange): List<NavPoint> {
        if (data.isEmpty()) return data
        
        val maxPoints = when (range) {
            TimeRange.WEEK_1 -> 7
            TimeRange.MONTH_1 -> 10
            TimeRange.MONTH_3 -> 12
            else -> 12 // 6M, 1Y, 3Y, ALL
        }

        if (data.size <= maxPoints) return data

        val step = (data.size - 1).toDouble() / (maxPoints - 1)
        val sampled = mutableListOf<NavPoint>()
        
        for (i in 0 until maxPoints - 1) {
            val index = (i * step).toInt()
            sampled.add(data[index])
        }
        // to ensure latest point is included
        sampled.add(data.last())
        return sampled
    }

    private fun loadFundDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            mfRepository.getFundDetail(schemeCode)
                .onSuccess { detail ->
                    _uiState.value = UiState.Success(detail)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to fetch fund details")
                }
        }
    }

    fun toggleFolderSelection(folderId: Long, fund: Fund) {
        viewModelScope.launch {
            val isCurrentlyInFolder = watchlistRepository.isFundInFolder(folderId, fund.schemeCode)
            if (isCurrentlyInFolder) {
                watchlistRepository.removeFundFromFolder(folderId, fund.schemeCode)
            } else {
                watchlistRepository.addFundToFolder(folderId, fund)
            }
        }
    }

    fun createFolderAndAddFund(folderName: String, fund: Fund) {
        viewModelScope.launch {
            val validName = folderName.trim()
            if (validName.isNotEmpty()) {
                val newFolderId = watchlistRepository.createFolder(validName)
                watchlistRepository.addFundToFolder(newFolderId, fund)
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _selectedRange.value = range
    }

    private fun filterNavHistory(
        history: List<NavPoint>,
        range: TimeRange
    ): List<NavPoint> {
        if (history.isEmpty()) return history
        if (range == TimeRange.INCEPTION || range.days == null) return history.reversed()

        val fmt     = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val cutoff  = LocalDate.now().minusDays(range.days.toLong())

        return history
            .reversed()
            .filter { point ->
                runCatching {
                    val date = LocalDate.parse(point.date, fmt)
                    !date.isBefore(cutoff)
                }.getOrDefault(true)
            }
    }
}
