package com.example.myapplication.data.repository

import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.LoanDebtEntity
import com.example.myapplication.data.local.entity.ProductEntity
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.LoanDebt
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.Transaction

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    accountType = accountType,
    accountNumberOrPhone = accountNumberOrPhone,
    currentBalance = currentBalance,
    currency = currency,
    isDefault = isDefault
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    accountType = accountType,
    accountNumberOrPhone = accountNumberOrPhone,
    currentBalance = currentBalance,
    currency = currency,
    isDefault = isDefault
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    barcode = barcode,
    buyingPrice = buyingPrice,
    sellingPrice = sellingPrice,
    currentStock = currentStock,
    minAlertStock = minAlertStock
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    barcode = barcode,
    buyingPrice = buyingPrice,
    sellingPrice = sellingPrice,
    currentStock = currentStock,
    minAlertStock = minAlertStock
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    accountId = accountId,
    productId = productId,
    type = type,
    amount = amount,
    profit = profit,
    description = description,
    referenceCode = referenceCode,
    timestamp = timestamp
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    accountId = accountId,
    productId = productId,
    type = type,
    amount = amount,
    profit = profit,
    description = description,
    referenceCode = referenceCode,
    timestamp = timestamp
)

fun LoanDebtEntity.toDomain(): LoanDebt = LoanDebt(
    id = id,
    personOrInstitution = personOrInstitution,
    phoneNumber = phoneNumber,
    direction = direction,
    principalAmount = principalAmount,
    remainingAmount = remainingAmount,
    dueDate = dueDate,
    status = status,
    createdAt = createdAt
)

fun LoanDebt.toEntity(): LoanDebtEntity = LoanDebtEntity(
    id = id,
    personOrInstitution = personOrInstitution,
    phoneNumber = phoneNumber,
    direction = direction,
    principalAmount = principalAmount,
    remainingAmount = remainingAmount,
    dueDate = dueDate,
    status = status,
    createdAt = createdAt
)
