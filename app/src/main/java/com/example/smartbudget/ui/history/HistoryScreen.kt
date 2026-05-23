package com.example.smartbudget.ui.history

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartbudget.data.AppDatabase
import com.example.smartbudget.data.Expense
import com.example.smartbudget.data.ExpenseRepository
import com.example.smartbudget.sms.SmsImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ExpenseRepository(AppDatabase.getInstance(app).expenseDao())

    val all: StateFlow<List<Expense>> = repo.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importState = MutableStateFlow<ImportState>(ImportState.Idle)

    fun importFromSms() {
        viewModelScope.launch {
            importState.value = ImportState.Loading
            val count = SmsImporter.importAll(getApplication(), repo)
            importState.value = ImportState.Done(count)
        }
    }
}

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Done(val count: Int) : ImportState()
}

@Composable
fun HistoryScreen(vm: HistoryViewModel = viewModel()) {
    val expenses by vm.all.collectAsState()
    val importState by vm.importState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("전체 거래 내역", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        ImportSection(importState = importState, onImport = { vm.importFromSms() })

        Spacer(modifier = Modifier.height(12.dp))

        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("거래 내역이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(expenses) { expense ->
                    HistoryItem(expense)
                }
            }
        }
    }
}

@Composable
fun ImportSection(importState: ImportState, onImport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("이전 거래 내역 불러오기", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    when (importState) {
                        is ImportState.Idle -> "문자함의 카드 결제 내역을 가져옵니다"
                        is ImportState.Loading -> "불러오는 중..."
                        is ImportState.Done -> "${importState.count}건 불러왔습니다"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onImport,
                enabled = importState !is ImportState.Loading
            ) {
                if (importState is ImportState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("불러오기")
                }
            }
        }
    }
}

@Composable
fun HistoryItem(expense: Expense) {
    val date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.KOREA).format(Date(expense.timestamp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.merchant, fontWeight = FontWeight.Medium)
                Text("${expense.category} · $date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${String.format("%,d", expense.amount)}원", fontWeight = FontWeight.Bold)
        }
    }
}
