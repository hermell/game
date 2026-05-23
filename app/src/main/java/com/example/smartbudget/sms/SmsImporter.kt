package com.example.smartbudget.sms

import android.content.Context
import android.provider.Telephony
import com.example.smartbudget.data.Expense
import com.example.smartbudget.data.ExpenseRepository

object SmsImporter {

    suspend fun importAll(context: Context, repo: ExpenseRepository): Int {
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE),
            null, null,
            "${Telephony.Sms.DATE} DESC"
        ) ?: return 0

        var count = 0
        cursor.use {
            val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
            while (it.moveToNext()) {
                val body = it.getString(bodyIdx) ?: continue
                val date = it.getLong(dateIdx)
                val parsed = SmsParser.parse(body) ?: continue
                repo.insert(
                    Expense(
                        amount = parsed.amount,
                        merchant = parsed.merchant,
                        category = parsed.category,
                        timestamp = date,
                        rawSms = body
                    )
                )
                count++
            }
        }
        return count
    }
}
