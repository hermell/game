package com.example.smartbudget.ui.budget

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartbudget.notification.BudgetAlertService

class BudgetViewModel(app: Application) : AndroidViewModel(app) {
    fun getBudget(): Long = BudgetAlertService.getBudget(getApplication())
    fun setBudget(amount: Long) = BudgetAlertService.setBudget(getApplication(), amount)
}

@Composable
fun BudgetScreen(vm: BudgetViewModel = viewModel()) {
    var input by remember { mutableStateOf(vm.getBudget().takeIf { it > 0 }?.toString() ?: "") }
    var saved by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("월 예산 설정", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter { c -> c.isDigit() }; saved = false },
            label = { Text("월 예산 (원)") },
            placeholder = { Text("예: 500000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            suffix = { Text("원") }
        )

        val amount = input.toLongOrNull() ?: 0L
        if (amount > 0) {
            Text(
                "${String.format("%,d", amount)}원",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = {
                vm.setBudget(amount)
                saved = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = amount > 0
        ) {
            Text("저장")
        }

        if (saved) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "예산이 저장되었습니다. 이 금액을 초과하면 알림을 드립니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
