package com.example.smartbudget.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartbudget.data.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(vm: CalendarViewModel = viewModel()) {
    val currentMonth by vm.currentMonth.collectAsState()
    val expenses by vm.monthExpenses.collectAsState()
    val selectedDay by vm.selectedDay.collectAsState()
    val selectedDayExpenses by vm.selectedDayExpenses.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        MonthHeader(
            year = currentMonth.year,
            month = currentMonth.month,
            onPrev = { vm.prevMonth() },
            onNext = { vm.nextMonth() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        WeekDayHeaders()
        Spacer(modifier = Modifier.height(4.dp))
        MonthGrid(
            year = currentMonth.year,
            month = currentMonth.month,
            expenses = expenses,
            selectedDay = selectedDay,
            onDayClick = { vm.selectDay(it) }
        )
        if (selectedDay != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "${currentMonth.month + 1}월 ${selectedDay}일",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (selectedDayExpenses.isEmpty()) {
                Text("거래 내역이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(selectedDayExpenses) { expense ->
                        SelectedDayExpenseItem(expense)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(year: Int, month: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "이전 달")
        }
        Text("${year}년 ${month + 1}월", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "다음 달")
        }
    }
}

@Composable
fun WeekDayHeaders() {
    val days = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthGrid(
    year: Int,
    month: Int,
    expenses: List<Expense>,
    selectedDay: Int?,
    onDayClick: (Int) -> Unit
) {
    val firstDayOfWeek = Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = Calendar.getInstance().apply { set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)

    val dayTotals = expenses.groupBy {
        Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_MONTH)
    }.mapValues { it.value.sumOf { e -> e.amount } }

    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1
                    if (day < 1 || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(48.dp))
                    } else {
                        val dayTotal = dayTotals[day] ?: 0L
                        val isSelected = day == selectedDay
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .clickable { onDayClick(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day.toString(), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                if (dayTotal > 0) {
                                    Text(
                                        formatShortAmount(dayTotal),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedDayExpenseItem(expense: Expense) {
    val time = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(expense.timestamp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(expense.merchant, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("${expense.category} · $time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${String.format("%,d", expense.amount)}원", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

private fun formatShortAmount(amount: Long): String = when {
    amount >= 10_000_000 -> "${amount / 10_000_000}천만"
    amount >= 1_000_000 -> "${amount / 10_000}만"
    amount >= 10_000 -> "${amount / 10_000}만"
    else -> "${amount / 1_000}천"
}
