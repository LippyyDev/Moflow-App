// data/local/entity/TransactionEntity.kt
package com.example.moflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moflow.domain.model.Transaction
import com.example.moflow.domain.model.TransactionCategory
import com.example.moflow.domain.model.TransactionType
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val notes: String
) {
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            type = TransactionType.valueOf(type),
            category = TransactionCategory.valueOf(category),
            date = Date(date),
            notes = notes
        )
    }

    companion object {
        fun fromTransaction(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                amount = transaction.amount,
                type = transaction.type.name,
                category = transaction.category.name,
                date = transaction.date.time,
                notes = transaction.notes
            )
        }
    }
}