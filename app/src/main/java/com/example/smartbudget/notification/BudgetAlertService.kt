package com.example.smartbudget.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.smartbudget.data.ExpenseRepository
import java.util.Calendar

object BudgetAlertService {

    private const val CHANNEL_ID = "budget_alert"
    private const val PREF_NAME = "budget_prefs"
    private const val KEY_BUDGET = "monthly_budget"

    fun getBudget(context: Context): Long =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getLong(KEY_BUDGET, 0L)

    fun setBudget(context: Context, amount: Long) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_BUDGET, amount).apply()
    }

    suspend fun checkAndNotify(context: Context, repo: ExpenseRepository) {
        val budget = getBudget(context)
        if (budget <= 0) return

        val now = Calendar.getInstance()
        val total = repo.getDayTotal(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        // 월 전체 합산
        val monthFlow = repo.getMonthTotal(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        // Flow를 직접 collect하지 않고 one-shot 방식 사용을 위해 DAO 직접 쿼리는 별도 메서드로 처리
        // 여기서는 단순히 알림 채널 생성 후 notify
        createChannel(context)
        sendNotification(context, "예산 초과 주의", "이번 달 지출이 설정 예산에 근접했습니다.")
    }

    fun notifyBudgetExceeded(context: Context, spent: Long, budget: Long) {
        createChannel(context)
        val msg = "이번 달 지출 ${formatAmount(spent)}원이 예산 ${formatAmount(budget)}원을 초과했습니다."
        sendNotification(context, "예산 초과!", msg)
    }

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(CHANNEL_ID, "예산 알림", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun formatAmount(amount: Long): String =
        String.format("%,d", amount)
}
