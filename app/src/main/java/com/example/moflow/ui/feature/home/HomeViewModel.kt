// ui/feature/home/HomeViewModel.kt
package com.example.moflow.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moflow.domain.model.Transaction
import com.example.moflow.domain.usecase.finance.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        getTransactions()
    }

    private fun getTransactions() {
        getTransactionsUseCase().onEach { transactions ->
            _state.value = state.value.copy(
                recentTransactions = transactions.take(5),
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }
}

data class HomeState(
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)