package com.example.myapplication.domain.model

data class DashboardSummary(
    val totalOutstandingDebt: Double = 0.0,
    val todaySales: Double = 0.0,
    val activeDebtorsCount: Int = 0
)
