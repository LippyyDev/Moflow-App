// ui/feature/finance/FinanceViewModel.kt
package com.example.moflow.ui.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moflow.domain.model.Transaction
import com.example.moflow.domain.model.TransactionCategory
import com.example.moflow.domain.model.TransactionType
import com.example.moflow.domain.usecase.finance.AddTransactionUseCase
import com.example.moflow.domain.usecase.finance.DeleteTransactionUseCase
import com.example.moflow.domain.usecase.finance.GetTransactionsUseCase
import com.example.moflow.domain.usecase.finance.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        getTransactions()
    }

    private fun getTransactions() {
        getTransactionsUseCase().onEach { transactions ->
            _state.value = state.value.copy(
                transactions = transactions,
                isLoading = false,
                filteredTransactions = filterTransactions(
                    transactions,
                    state.value.selectedMonth,
                    state.value.selectedCategory,
                    state.value.selectedType
                ),
                monthlyIncome = calculateMonthlyAmount(transactions, TransactionType.INCOME, state.value.selectedMonth),
                monthlyExpense = calculateMonthlyAmount(transactions, TransactionType.EXPENSE, state.value.selectedMonth)
            )
        }.launchIn(viewModelScope)
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onCategoryChange(category: TransactionCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDateChange(date: Date) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onMonthFilterChange(month: Int) {
        _state.update {
            it.copy(
                selectedMonth = month,
                filteredTransactions = filterTransactions(
                    it.transactions,
                    month,
                    it.selectedCategory,
                    it.selectedType
                ),
                monthlyIncome = calculateMonthlyAmount(it.transactions, TransactionType.INCOME, month),
                monthlyExpense = calculateMonthlyAmount(it.transactions, TransactionType.EXPENSE, month)
            )
        }
    }

    fun onCategoryFilterChange(category: TransactionCategory?) {
        _state.update {
            it.copy(
                selectedCategory = category,
                filteredTransactions = filterTransactions(
                    it.transactions,
                    it.selectedMonth,
                    category,
                    it.selectedType
                )
            )
        }
    }

    fun onTypeFilterChange(type: TransactionType?) {
        _state.update {
            it.copy(
                selectedType = type,
                filteredTransactions = filterTransactions(
                    it.transactions,
                    it.selectedMonth,
                    it.selectedCategory,
                    type
                )
            )
        }
    }

    fun addTransaction() {
        viewModelScope.launch {
            try {
                val amount = _uiState.value.amount.toDoubleOrNull() ?: return@launch
                val transaction = Transaction(
                    amount = amount,
                    type = _uiState.value.type,
                    category = _uiState.value.category,
                    date = _uiState.value.date,
                    notes = _uiState.value.notes
                )
                addTransactionUseCase(transaction)
                resetTransactionForm()
                _state.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateTransaction() {
        viewModelScope.launch {
            try {
                val amount = _uiState.value.amount.toDoubleOrNull() ?: return@launch
                val transaction = Transaction(
                    id = _uiState.value.id,
                    amount = amount,
                    type = _uiState.value.type,
                    category = _uiState.value.category,
                    date = _uiState.value.date,
                    notes = _uiState.value.notes
                )
                updateTransactionUseCase(transaction)
                resetTransactionForm()
                _state.update { it.copy(showEditDialog = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transaction)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun openAddDialog() {
        resetTransactionForm()
        _state.update { it.copy(showAddDialog = true) }
    }

    fun closeAddDialog() {
        _state.update { it.copy(showAddDialog = false) }
    }

    fun openEditDialog(transaction: Transaction) {
        _uiState.update {
            it.copy(
                id = transaction.id,
                amount = transaction.amount.toString(),
                type = transaction.type,
                category = transaction.category,
                date = transaction.date,
                notes = transaction.notes
            )
        }
        _state.update { it.copy(showEditDialog = true) }
    }

    fun closeEditDialog() {
        _state.update { it.copy(showEditDialog = false) }
    }

    private fun resetTransactionForm() {
        _uiState.update {
            TransactionUiState(
                date = Calendar.getInstance().time
            )
        }
    }

    private fun filterTransactions(
        transactions: List<Transaction>,
        month: Int,
        category: TransactionCategory?,
        type: TransactionType?
    ): List<Transaction> {
        return transactions.filter { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.date }
            val transactionMonth = calendar.get(Calendar.MONTH)

            val monthMatches = month == -1 || transactionMonth == month
            val categoryMatches = category == null || transaction.category == category
            val typeMatches = type == null || transaction.type == type

            monthMatches && categoryMatches && typeMatches
        }
    }

    // ui/feature/finance/FinanceViewModel.kt (continued)
    private fun calculateMonthlyAmount(
        transactions: List<Transaction>,
        type: TransactionType,
        month: Int
    ): Double {
        return transactions
            .filter {
                val calendar = Calendar.getInstance().apply { time = it.date }
                val transactionMonth = calendar.get(Calendar.MONTH)
                it.type == type && (month == -1 || transactionMonth == month)
            }
            .sumOf { it.amount }
    }
}

data class FinanceState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedCategory: TransactionCategory? = null,
    val selectedType: TransactionType? = null,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val error: String? = null
)

data class TransactionUiState(
    val id: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val date: Date = Calendar.getInstance().time,
    val notes: String = ""
)