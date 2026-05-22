package com.example.smartbudget.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbudget.data.AppDatabase
import com.example.smartbudget.data.Expense
import com.example.smartbudget.data.ExpenseRepository
import com.example.smartbudget.notification.BudgetAlertService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ExpenseRepository(AppDatabase.getInstance(app).expenseDao())
    private val now = Calendar.getInstance()
    private val year = now.get(Calendar.YEAR)
    private val month = now.get(Calendar.MONTH)

    val monthExpenses: StateFlow<List<Expense>> = repo.getMonthExpenses(year, month)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthTotal: StateFlow<Long> = repo.getMonthTotal(year, month)
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun getBudget(): Long = BudgetAlertService.getBudget(getApplication())

    fun setBudget(amount: Long) {
        BudgetAlertService.setBudget(getApplication(), amount)
    }

    fun addManualExpense(amount: Long, merchant: String, category: String) {
        viewModelScope.launch {
            repo.insert(
                Expense(
                    amount = amount,
                    merchant = merchant,
                    category = category,
                    timestamp = System.currentTimeMillis(),
                    rawSms = ""
                )
            )
        }
    }
}
