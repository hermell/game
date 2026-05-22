package com.example.smartbudget.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbudget.data.AppDatabase
import com.example.smartbudget.data.Expense
import com.example.smartbudget.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CalendarMonth(val year: Int, val month: Int)

class CalendarViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ExpenseRepository(AppDatabase.getInstance(app).expenseDao())

    private val now = Calendar.getInstance()
    val currentMonth = MutableStateFlow(CalendarMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH)))

    val monthExpenses: StateFlow<List<Expense>> = currentMonth.flatMapLatest { (y, m) ->
        repo.getMonthExpenses(y, m)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDay = MutableStateFlow<Int?>(null)
    val selectedDayExpenses = MutableStateFlow<List<Expense>>(emptyList())

    fun selectDay(day: Int) {
        selectedDay.value = day
        viewModelScope.launch {
            val (y, m) = currentMonth.value
            selectedDayExpenses.value = repo.getMonthExpenses(y, m).stateIn(
                viewModelScope, SharingStarted.Eagerly, emptyList()
            ).value.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.DAY_OF_MONTH) == day
            }
        }
    }

    fun prevMonth() {
        val (y, m) = currentMonth.value
        currentMonth.value = if (m == 0) CalendarMonth(y - 1, 11) else CalendarMonth(y, m - 1)
        selectedDay.value = null
    }

    fun nextMonth() {
        val (y, m) = currentMonth.value
        currentMonth.value = if (m == 11) CalendarMonth(y + 1, 0) else CalendarMonth(y, m + 1)
        selectedDay.value = null
    }

    fun getDayTotal(day: Int): Long {
        val (y, m) = currentMonth.value
        return monthExpenses.value.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.YEAR) == y && cal.get(Calendar.MONTH) == m && cal.get(Calendar.DAY_OF_MONTH) == day
        }.sumOf { it.amount }
    }
}
