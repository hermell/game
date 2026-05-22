package com.example.smartbudget.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.smartbudget.data.AppDatabase
import com.example.smartbudget.data.Expense
import com.example.smartbudget.data.ExpenseRepository
import com.example.smartbudget.notification.BudgetAlertService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullBody = messages.joinToString("") { it.messageBody }

        val parsed = SmsParser.parse(fullBody) ?: return

        val db = AppDatabase.getInstance(context)
        val repo = ExpenseRepository(db.expenseDao())

        CoroutineScope(Dispatchers.IO).launch {
            val expense = Expense(
                amount = parsed.amount,
                merchant = parsed.merchant,
                category = parsed.category,
                timestamp = System.currentTimeMillis(),
                rawSms = fullBody
            )
            repo.insert(expense)
            BudgetAlertService.checkAndNotify(context, repo)
        }
    }
}
