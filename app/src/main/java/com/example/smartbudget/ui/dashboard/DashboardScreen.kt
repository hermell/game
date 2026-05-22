package com.example.smartbudget.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartbudget.data.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(vm: DashboardViewModel = viewModel()) {
    val expenses by vm.monthExpenses.collectAsState()
    val total by vm.monthTotal.collectAsState()
    val budget = remember { mutableLongStateOf(vm.getBudget()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("이번 달 지출 현황", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        item {
            SpendingSummaryCard(total = total, budget = budget.longValue)
        }
        item {
            CategoryDonutChart(expenses = expenses)
        }
        item {
            Text("최근 거래", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        items(expenses.take(5)) { expense ->
            ExpenseItem(expense)
        }
    }
}

@Composable
fun SpendingSummaryCard(total: Long, budget: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("총 지출", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${String.format("%,d", total)}원", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            if (budget > 0) {
                val progress = (total.toFloat() / budget).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    "예산 ${String.format("%,d", budget)}원 중 ${(progress * 100).toInt()}% 사용",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryDonutChart(expenses: List<Expense>) {
    val categoryTotals = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
    val total = categoryTotals.values.sum().takeIf { it > 0 } ?: return

    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
        Color(0xFF9C27B0), Color(0xFFF44336), Color(0xFF00BCD4)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("카테고리별 지출", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    var startAngle = -90f
                    categoryTotals.values.forEachIndexed { i, amount ->
                        val sweep = (amount.toFloat() / total) * 360f
                        drawArc(
                            color = colors[i % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 28f),
                            topLeft = Offset(14f, 14f),
                            size = Size(size.width - 28f, size.height - 28f)
                        )
                        startAngle += sweep
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categoryTotals.entries.forEachIndexed { i, (cat, amt) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Canvas(modifier = Modifier.size(10.dp)) {
                                drawCircle(colors[i % colors.size])
                            }
                            Text("$cat ${String.format("%,d", amt)}원", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    val date = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA).format(Date(expense.timestamp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(expense.merchant, fontWeight = FontWeight.Medium)
                Text("${expense.category} · $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${String.format("%,d", expense.amount)}원", fontWeight = FontWeight.Bold)
        }
    }
}
