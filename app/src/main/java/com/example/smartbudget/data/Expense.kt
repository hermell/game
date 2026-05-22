package com.example.smartbudget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Long,
    val merchant: String,
    val category: String,
    val timestamp: Long,
    val rawSms: String
)
