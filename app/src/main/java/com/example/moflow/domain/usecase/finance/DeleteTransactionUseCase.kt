// domain/usecase/finance/DeleteTransactionUseCase.kt
package com.example.moflow.domain.usecase.finance

import com.example.moflow.domain.model.Transaction
import com.example.moflow.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }
}