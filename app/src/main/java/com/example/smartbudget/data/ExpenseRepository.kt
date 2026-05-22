package com.example.smartbudget.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(private val dao: ExpenseDao) {

    fun getAllExpenses(): Flow<List<Expense>> = dao.getAllExpenses()

    fun getMonthExpenses(year: Int, month: Int): Flow<List<Expense>> {
        val (start, end) = monthRange(year, month)
        return dao.getExpensesBetween(start, end)
    }

    fun getMonthTotal(year: Int, month: Int): Flow<Long?> {
        val (start, end) = monthRange(year, month)
        return dao.getTotalBetween(start, end)
    }

    fun getDayExpenses(year: Int, month: Int, day: Int): Flow<List<Expense>> {
        val (start, end) = dayRange(year, month, day)
        return dao.getExpensesBetween(start, end)
    }

    suspend fun getDayTotal(year: Int, month: Int, day: Int): Long {
        val (start, end) = dayRange(year, month, day)
        return dao.getExpensesBetweenOnce(start, end).sumOf { it.amount }
    }

    suspend fun insert(expense: Expense) = dao.insert(expense)

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(year, month + 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return start to end
    }

    private fun dayRange(year: Int, month: Int, day: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(year, month, day + 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return start to end
    }
}
