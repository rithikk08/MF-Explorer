package com.utilityhub.mfexplorer.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utilityhub.mfexplorer.domain.model.Fund
import com.utilityhub.mfexplorer.domain.model.FundCategory
import com.utilityhub.mfexplorer.domain.repository.MfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreState(
    val categoryFunds: Map<FundCategory, List<Fund>> = emptyMap(),
    val loadingCategories: Set<FundCategory> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val mfRepository: MfRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state = _state.asStateFlow()

    init { 
        loadAllCategories() 
    }

    private fun loadAllCategories() {
        FundCategory.entries.forEach { category ->
            viewModelScope.launch {
                _state.update { it.copy(loadingCategories = it.loadingCategories + category) }
                
                mfRepository.getFundsByCategory(category)
                    .onSuccess { funds ->
                        _state.update { state ->
                            state.copy(
                                categoryFunds = state.categoryFunds + (category to funds.take(4)),
                                loadingCategories = state.loadingCategories - category
                            )
                        }
                    }
                    .onFailure { error ->
                        _state.update { state -> 
                            state.copy(
                                error = error.message ?: "An unknown error occurred",
                                loadingCategories = state.loadingCategories - category
                            ) 
                        }
                    }
            }
        }
    }
}
