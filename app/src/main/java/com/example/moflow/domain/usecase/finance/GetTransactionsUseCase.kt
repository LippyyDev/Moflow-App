// domain/usecase/finance/GetTransactionsUseCase.kt
package com.example.moflow.domain.usecase.finance

import com.example.moflow.domain.model.Transaction
import com.example.moflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }
}