// data/local/database/MoFlowDatabase.kt
package com.example.moflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moflow.data.local.dao.TransactionDao
import com.example.moflow.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MoFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}