package com.utilityhub.mfexplorer.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.domain.repository.MfRepository
import com.utilityhub.mfexplorer.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val mfRepository: MfRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<Fund>>>(UiState.Empty)
    val uiState = _uiState.asStateFlow()

    init {
        _searchQuery
            .onEach { query ->
                if (query.trim().length < 2) {
                    _uiState.value = UiState.Empty
                }
            }
            .debounce(300L)
            .distinctUntilChanged()
            .filter { it.trim().length >= 2 }
            .onEach { query ->
                _uiState.value = UiState.Loading
                mfRepository.searchFunds(query)
                    .onSuccess { funds ->
                        _uiState.value = if (funds.isEmpty()) UiState.Empty
                        else UiState.Success(funds)
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Unknown error occurred")
                    }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
