package com.example.myapplication.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
    DEBT_BORROW,
    DEBT_REPAY,
    
    // Retained for backwards compatibility with existing features
    CREDIT,
    PAYMENT,
    CASH_SALE
}
