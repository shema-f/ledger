# Project Plan

Imari - Smart Financial Ledger & Business Operating System for Merchants in Rwanda. Features include: 1. Multi-Account Hub: Track MTN MoMo, Airtel Money, Bank of Kigali (BK), I&M Bank, Equity Bank, BPR, Urwego SACCO, and Cash Wallets. 2. Multi-Rail Rwandan SMS Ingestion Engine: Intercept & parse MTN MoMo, BK Alerts, Airtel Money transactions with auto-reconciliation interactive bottom sheet. 3. Inventory & POS Lite: Product catalog, stock management, buying vs selling prices, profit margins. 4. Debt & Loan Tracker: Customer Debts (Amadeni) vs Business Loans (Inguzanyo). 5. Business Reports & Instant PDF Export: P&L income statement, COGS, cash-flow, and PDF statement generator. 6. Google One-Tap & Offline PIN Auth. 7. Deep Emerald Teal (#006A4E), Slate, and White UI with custom Imari logo and favicon app icon.

## Project Brief

# Project Brief: Imari - Smart Financial Ledger & Business Operating System

## Features
1. **Rwandan Payment SMS Ingestion & Account Reconciliation**: Intercept and parse incoming transaction alerts from MTN MoMo, Airtel Money, and local banks (BK, I&M, Equity, BPR, SACCO) with an interactive bottom sheet for instant multi-account reconciliation.
2. **Inventory & POS Lite**: Manage product catalogs, stock levels, buying versus selling prices, and automatically calculate real-time profit margins during sales.
3. **Debt & Loan Tracker (Amadeni & Inguzanyo)**: Track customer credit debts and business loan balances with record logging and localized ledger management.
4. **Financial Reporting & PDF Generator**: View real-time Profit & Loss (P&L) statements, COGS, and cash flow with instant formatted PDF export.
5. **Security & Offline Auth**: Secure app access using Google One-Tap authentication alongside offline PIN entry for seamless offline usage.

## High-Level Tech Stack
* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3 with Deep Emerald Teal `#006A4E`, Slate, and White theme)
* **Navigation**: Jetpack Navigation 3 (state-driven)
* **Adaptive Strategy**: Compose Material Adaptive library
* **Architecture & Async**: MVVM Architecture, Kotlin Coroutines, and Flow
* **Core Components**: Android Telephony API / BroadcastReceiver (for payment SMS processing) and `PdfDocument` API (for statement generation)

## Implementation Steps
**Total Duration:** 25m 53s

### Task_1_ImariDatabaseAndMultiAccountSchema: Implement Imari Room Schema (AccountEntity, ProductEntity, TransactionEntity, LoanDebtEntity), DAOs, Repositories, and pre-seeded Rwandan financial account rails (MTN MoMo, Airtel, BK, I&M, Equity, BPR, SACCO, Cash).
- **Status:** COMPLETED
- **Updates:** Implemented Imari Room schema (AccountEntity, ProductEntity, TransactionEntity, LoanDebtEntity), DAOs (AccountDao, ProductDao, TransactionDao, LoanDebtDao), ImariDatabase with pre-seeded Rwandan rails (MTN MoMo, BK, Airtel, I&M, Equity, BPR, Urwego SACCO, Cash Register), ImariConverters, Repository layer (AccountRepository, ProductRepository, TransactionRepository, LoanDebtRepository), and ImariRepositoriesTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Room Database entities Account, Product, Transaction, LoanDebt implemented
  - DAOs and Repositories created with Kotlin Flow streams
  - Default Rwandan bank and telecom accounts pre-seeded
  - Unit tests pass
  - build pass

### Task_2_MultiRailRwandanSMSParserAndReconciliation: Implement RwandaFinancialParser regex engine for MTN MoMo, Bank of Kigali (BK), and Airtel Money, with an interactive bottom sheet for real-time transaction reconciliation.
- **Status:** COMPLETED
- **Updates:** Implemented RwandaFinancialParser multi-rail regex engine (MTN MoMo, Bank of Kigali BK, Airtel Money, I&M Bank), updated MomoSmsReceiver and MomoNotificationService with PaymentAlertManager, created interactive SmsReconciliationBottomSheet UI with 3 assignment options (Product POS Sale, Settle Customer Debt/Loan, General Revenue Deposit), created SmsReconciliationViewModel, and implemented unit tests RwandaFinancialParserTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - RwandaFinancialParser extracts provider, amount, reference code, balance, and sender
  - Interactive Bottom Sheet for SMS alert reconciliation implemented
  - NotificationListenerService and BroadcastReceiver updated for multi-rail parsing
  - Unit tests for RwandaFinancialParser pass
  - build pass
- **Duration:** 12m 35s

### Task_3_InventoryPOSAndDebtTracker: Build Inventory & POS Lite UI (product catalog, buying vs selling price, stock, profit margins, POS sale checkout) and Split Debt & Loan Tracker (Amadeni y'abakiriya vs Inguzanyo z'ubucuruzi).
- **Status:** COMPLETED
- **Updates:** Implemented InventoryViewModel, LoanDebtViewModel, InventoryScreen with low-stock alerts, unit profit margin calculation, and POS Checkout Modal (deducting stock, logging profit & INCOME), LoanDebtScreen with dual tabs (Customer Debts - RECEIVABLE vs Business Loans - PAYABLE), summary cards, and Repayment Modal (updating balance & logging DEBT_REPAY), updated AppNavigation with tabs, and created unit tests InventoryViewModelTest and LoanDebtViewModelTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Inventory catalog UI with stock alerts and profit margin calculation working
  - POS checkout reduces stock and records transaction with profit
  - Split Debt & Loan Tracker displays receivables vs payables with repayment log
  - build pass
- **Duration:** 6m 25s

### Task_4_BusinessReportsPDFAndGoogleAuth: Build Business Reports screen (P&L, COGS, Gross Profit, Uncollected Debts) with PDF Statement Export using PdfDocument API, and Google Auth/PIN Security screen.
- **Status:** COMPLETED
- **Updates:** Implemented PdfReportGenerator using Android PdfDocument API for monthly financial audit statements, ReportsViewModel & ReportsScreen (P&L cards, COGS, Gross Profit, date range filter, wallet balances breakdown, PDF statement share intent), AuthViewModel & ImariAuthScreen (Google One-Tap button, offline PIN keypad, Merchant Onboarding modal with Shop Name & Sector), registered FileProvider in AndroidManifest.xml, and created unit tests ReportsViewModelTest & AuthViewModelTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Business Reports screen computes net revenue, COGS, gross profit, and debts
  - PdfDocument API exports downloadable monthly audit PDF statement
  - Google One-Tap / PIN Auth screen implemented
  - build pass
- **Duration:** 6m 53s

### Task_5_BrandingThemeAndVerification: Apply Imari Deep Emerald Teal (#006A4E), Slate & White theme, vector logo & app icon, execute unit test suite, verify assembleDebug build, and push to GitHub.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Imari Deep Emerald Teal logo and app icon rendered
  - All unit tests pass
  - Build compiles with zero errors
  - Changes committed and pushed to https://github.com/shema-f/ledger.git
- **StartTime:** 2026-09-09 15:15:17 SAST

