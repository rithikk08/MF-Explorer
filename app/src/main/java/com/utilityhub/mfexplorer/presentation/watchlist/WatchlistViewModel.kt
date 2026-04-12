package com.utilityhub.mfexplorer.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utilityhub.mfexplorer.domain.model.WatchlistFolder
import com.utilityhub.mfexplorer.domain.repository.WatchlistRepository
import com.utilityhub.mfexplorer.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    val uiState: StateFlow<UiState<List<WatchlistFolder>>> = watchlistRepository.getAllFolders()
        .map { folders ->
            if (folders.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(folders)
            }
        }
        .catch { error ->
            emit(UiState.Error(error.message ?: "Failed to load watchlists"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            watchlistRepository.deleteFolder(folderId)
        }
    }

    fun updateFolderName(folderId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            watchlistRepository.updateFolderName(folderId, newName)
        }
    }
}
