# Project Plan

Kayi y'Ideni - Smart Merchant Ledger & Automatic MoMo Reconciler for local merchants. Features: 1. Customer & Debt Management: Track customer balances, debts, and payments with Kinyarwanda/English/French support. 2. Automated MoMo & SMS Transaction Parser: Intercept MTN MoMo and Airtel Money payment notifications/SMS using NotificationListenerService and BroadcastReceiver with regex parsing for RWF amounts, customer names, phone numbers, and TxIDs. 3. Auto-Reconciliation: Automatically match incoming MoMo payments to open customer debts. 4. Debt Reminders & Payment Requests: Send pre-filled SMS/WhatsApp debt collection reminders in Kinyarwanda/English. 5. Modern Material 3 Merchant Dashboard: Daily sales overview, outstanding debt summaries, search, transaction history, and privacy-first local encrypted database.

## Project Brief

# Project Brief: Kayi y'Ideni

Kayi y'Ideni is a smart merchant ledger and automatic mobile money (MoMo) reconciler designed for local micro-merchants. The application simplifies customer debt tracking, automatically intercepts incoming mobile payment notifications, and reconciles pending customer balances using a privacy-first, offline-ready architecture.

---

## Features

1. **Customer & Debt Ledger Management**  
   Maintain individual customer profiles, debt entries, and payment records with localized multi-language UI support (Kinyarwanda, English, and French).

2. **Automated MoMo & SMS Transaction Parser**  
   Intercept incoming MTN MoMo and Airtel Money payment notifications and SMS in real time using `NotificationListenerService` and `BroadcastReceiver`, extracting RWF transaction amounts, customer names, phone numbers, and TxIDs via regex parsing.

3. **Auto-Reconciliation Engine**  
   Automatically match incoming verified MoMo payments to open customer debts to update ledger balances without manual intervention.

4. **Debt Collection Reminders & Merchant Dashboard**  
   Display daily sales summaries and total outstanding debts on a Material 3 dashboard, while enabling merchants to dispatch pre-filled debt collection reminders via SMS or WhatsApp in Kinyarwanda or English.

---

## High-Level Tech Stack

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose with Material Design 3
* **Navigation & Adaptive Layouts**: Strictly Jetpack Navigation 3 (state-driven) and Compose Material Adaptive library
* **Asynchronous Data & Concurrency**: Kotlin Coroutines & Flow
* **Background Interception**: Android `NotificationListenerService` & `BroadcastReceiver`
* **Local Persistence & Security**: Encrypted Room Database (SQLCipher) for privacy-first, secure local ledger storage

## Implementation Steps
**Total Duration:** 32m 56s

### Task_1_LedgerCoreAndEntities: Implement Room Database Schema (Customer, LedgerRecord, MoMoLog entities), DAOs, Repository layer, and localized domain models for Kayi y'Ideni ledger.
- **Status:** COMPLETED
- **Updates:** Implemented Room Database schema (CustomerEntity, LedgerRecordEntity, MoMoLogEntity), DAOs (CustomerDao, LedgerRecordDao, MoMoLogDao), KayiDatabase, TypeConverters, Domain models (Customer, LedgerRecord, MoMoLog, TransactionType, DashboardSummary), LedgerRepository, MoMoRepository, Mappers, and unit tests (LedgerRepositoryTest). Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Customer, LedgerRecord, and MoMoLog entities and Room DAOs created
  - Repository layer for ledger and MoMo logs implemented
  - Unit tests for ledger queries pass
  - build pass

### Task_2_MoMoNotificationAndSmsEngine: Implement NotificationListenerService (MomoNotificationService), BroadcastReceiver (MomoSmsReceiver), MomoParser regex engine for MTN MoMo and Airtel Money, and Auto-Reconciliation engine.
- **Status:** COMPLETED
- **Updates:** Implemented MomoParser regex engine for MTN MoMo and Airtel Money (Kinyarwanda & English), MomoNotificationService (NotificationListenerService), MomoSmsReceiver (BroadcastReceiver), AutoReconciliationEngine with phone/name matching & debt settlement, and AndroidManifest permissions & registrations. Created unit tests MomoParserTest and AutoReconciliationEngineTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - MomoNotificationService & MomoSmsReceiver registered in Manifest
  - Regex parser extracts RWF amounts, customer names, phone numbers, TxIDs
  - Auto-reconciliation engine matches incoming payments to open customer debts
  - Unit tests for regex parser and auto-reconciliation pass
  - build pass
- **Duration:** 25m 34s

### Task_3_MerchantDashboardAndLedgerUI: Build modern Jetpack Compose Material 3 UI: Merchant Dashboard, Customer Ledger & Debt detail screens, Kinyarwanda/English debt collection SMS reminders, MoMo Live Feed & Manual Reconciliation, and Notification Access onboarding.
- **Status:** COMPLETED
- **Updates:** Implemented DashboardViewModel, CustomerViewModel, MoMoFeedViewModel, Jetpack Compose Material 3 UI (DashboardScreen, CustomerListScreen, CustomerDetailScreen with multilingual Kinyarwanda/English/French debt reminders, MoMoLiveFeedScreen, NotificationOnboardingScreen), Navigation, and unit tests (DashboardViewModelTest, CustomerViewModelTest, MoMoFeedViewModelTest). Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Material 3 Dashboard displaying daily sales and outstanding debt metrics
  - Customer Ledger screen with debt addition, settlement, and SMS/WhatsApp reminders
  - MoMo Live Feed displaying incoming payment logs and reconciliation status
  - Notification Access onboarding screen with clear explanation
  - build pass
- **Duration:** 7m 22s

### Task_4_VerificationAndQualityGate: Perform end-to-end testing, unit test suite execution, build compilation, and critic agent UI/stability verification.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - All unit tests pass
  - Build compiles without errors
  - Application launches cleanly and operates stably
- **StartTime:** 2026-09-09 13:47:45 SAST

