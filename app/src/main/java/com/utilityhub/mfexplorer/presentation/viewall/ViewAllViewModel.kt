package com.utilityhub.mfexplorer.presentation.viewall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.domain.model.FundCategory
import com.utilityhub.mfexplorer.domain.repository.MfRepository
import com.utilityhub.mfexplorer.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewAllViewModel @Inject constructor(
    private val mfRepository: MfRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val rawCategoryName: String = checkNotNull(savedStateHandle["category"])

    val category: FundCategory = try {
        FundCategory.valueOf(rawCategoryName)
    } catch (e: IllegalArgumentException) {
        FundCategory.INDEX
    }

    private val _uiState = MutableStateFlow<UiState<List<Fund>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadFunds()
    }

    private fun loadFunds() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = if (rawCategoryName == "ALL") {
                mfRepository.getAllFunds()
            } else {
                mfRepository.getFundsByCategory(category)
            }
            
            result
                .onSuccess { funds ->
                    if (funds.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(funds)
                    }
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to fetch funds.")
                }
        }
    }
}
