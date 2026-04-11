package com.utilityhub.mfexplorer.presentation.watchlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utilityhub.mfexplorer.domain.model.WatchlistFund
import com.utilityhub.mfexplorer.domain.repository.WatchlistRepository
import com.utilityhub.mfexplorer.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistDetailViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val folderId: Long = checkNotNull(savedStateHandle.get<String>("folderId")?.toLongOrNull())

    private val _folderName = MutableStateFlow("Portfolio")
    val folderName = _folderName.asStateFlow()

    private val _funds = MutableStateFlow<UiState<List<WatchlistFund>>>(UiState.Loading)
    val funds = _funds.asStateFlow()

    init {
        viewModelScope.launch {
            watchlistRepository.getAllFolders().collect { folders ->
                val folder = folders.find { it.id == folderId }
                if (folder != null) {
                    _folderName.value = folder.name
                }
            }
        }

        viewModelScope.launch {
            watchlistRepository.getFundsInFolder(folderId)
                .catch { e ->
                    _funds.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { list ->
                    if (list.isEmpty()) {
                        _funds.value = UiState.Empty
                    } else {
                        _funds.value = UiState.Success(list)
                    }
                }
        }
    }
}
