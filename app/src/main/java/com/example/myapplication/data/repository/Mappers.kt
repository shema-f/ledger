package com.example.myapplication.data.repository

import com.example.myapplication.data.local.entity.CustomerEntity
import com.example.myapplication.data.local.entity.LedgerRecordEntity
import com.example.myapplication.data.local.entity.MoMoLogEntity
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.LedgerRecord
import com.example.myapplication.domain.model.MoMoLog

fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    fullName = fullName,
    phoneNumber = phoneNumber,
    nickname = nickname,
    totalDebt = totalDebt,
    createdAt = createdAt
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    fullName = fullName,
    phoneNumber = phoneNumber,
    nickname = nickname,
    totalDebt = totalDebt,
    createdAt = createdAt
)

fun LedgerRecordEntity.toDomain(): LedgerRecord = LedgerRecord(
    id = id,
    customerId = customerId,
    type = type,
    amount = amount,
    description = description,
    momoTxId = momoTxId,
    dueDate = dueDate,
    timestamp = timestamp
)

fun LedgerRecord.toEntity(): LedgerRecordEntity = LedgerRecordEntity(
    id = id,
    customerId = customerId,
    type = type,
    amount = amount,
    description = description,
    momoTxId = momoTxId,
    dueDate = dueDate,
    timestamp = timestamp
)

fun MoMoLogEntity.toDomain(): MoMoLog = MoMoLog(
    id = id,
    senderName = senderName,
    senderPhone = senderPhone,
    amount = amount,
    txId = txId,
    balanceAfter = balanceAfter,
    rawText = rawText,
    timestamp = timestamp,
    isReconciled = isReconciled
)

fun MoMoLog.toEntity(): MoMoLogEntity = MoMoLogEntity(
    id = id,
    senderName = senderName,
    senderPhone = senderPhone,
    amount = amount,
    txId = txId,
    balanceAfter = balanceAfter,
    rawText = rawText,
    timestamp = timestamp,
    isReconciled = isReconciled
)
