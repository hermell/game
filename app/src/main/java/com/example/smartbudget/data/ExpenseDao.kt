package com.example.smartbudget.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startMs AND timestamp < :endMs ORDER BY timestamp DESC")
    fun getExpensesBetween(startMs: Long, endMs: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startMs AND timestamp < :endMs")
    fun getTotalBetween(startMs: Long, endMs: Long): Flow<Long?>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startMs AND timestamp < :endMs ORDER BY timestamp DESC")
    suspend fun getExpensesBetweenOnce(startMs: Long, endMs: Long): List<Expense>
}
