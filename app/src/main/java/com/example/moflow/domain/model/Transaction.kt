// domain/model/Transaction.kt
package com.example.moflow.domain.model

import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val date: Date,
    val notes: String = ""
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class TransactionCategory {
    FOOD, TRANSPORT, ENTERTAINMENT, SALARY, GIFT, OTHER
}