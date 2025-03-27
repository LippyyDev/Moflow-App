// data/repository/TransactionRepositoryImpl.kt
package com.example.moflow.data.repository

import com.example.moflow.data.local.dao.TransactionDao
import com.example.moflow.data.local.entity.TransactionEntity
import com.example.moflow.domain.model.Transaction
import com.example.moflow.domain.model.TransactionCategory
import com.example.moflow.domain.model.TransactionType
import com.example.moflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionById(id: String): Flow<Transaction?> {
        return transactionDao.getTransactionById(id).map { entity ->
            entity?.toTransaction()
        }
    }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type.name).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionsByCategory(category: TransactionCategory): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category.name).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate.time, endDate.time).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(TransactionEntity.fromTransaction(transaction))
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(TransactionEntity.fromTransaction(transaction))
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(TransactionEntity.fromTransaction(transaction))
    }

    override suspend fun deleteTransactionById(id: String) {
        transactionDao.deleteTransactionById(id)
    }
}