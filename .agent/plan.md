# Project Plan

IFARANGA - Your money. One place. (Amafaranga yawe. Uyamenye.) The Financial Operating System for Rwanda. Features include: 1. Financial Command Center Home & Daily Financial Pulse (+RWF In, -RWF Out, Net Movement with live pulse animation). 2. Money Flow: Money In -> Saved / Spent -> Retained visualization with category breakdowns. 3. IFARANGA Intelligence & 30-Day Forecasting: Embedded smart insights and cash flow shortage alerts. 4. Financial Health Score (e.g. 82/100 Strong with Cash Flow, Debt, Savings, Expenses breakdown). 5. Business & POS Lite: Barcode scanning, Inventory stock alerts, Profit tracking, Customer Debts (Amadeni) & Business Loans (Inguzanyo). 6. Multi-Rail Automatic Transaction Capture & Multi-Account Hub (MTN MoMo, Airtel, BK, I&M, Equity, BPR, SACCO, Cash). 7. 5-Tab Navigation (Home, Money, Business, Insights, Profile) + Central Quick Action Floating (+) Button. 8. Dark Fintech UI + Subtle African Geometric Identity with abstract geometric I/M emblem logo. 9. Trilingual Support: English, Kinyarwanda, French (EN / RW / FR). 10. Instant PDF Statement Export & Shareable Business Reports.

## Project Brief

# Project Brief: IFARANGA - Financial Operating System

## Features

1. **Financial Command Center & Multi-Account Hub**: Real-time dashboard showing daily financial pulse (+RWF In, -RWF Out, Net Movement) aggregated across mobile money (MTN MoMo, Airtel) and bank accounts (BK, I&M, Equity, BPR, SACCO, Cash).
2. **Money Flow & Health Insights**: Visual cash flow tracking (Saved vs. Spent vs. Retained) with category breakdowns, a Financial Health Score, and 30-day forecasting with cash shortage alerts.
3. **Business & POS Lite**: Lightweight point-of-sale capabilities featuring barcode scanning, inventory stock alerts, profit tracking, and customer debt (Amadeni) & loan tracking.
4. **Adaptive 5-Tab Navigation & Quick Action Center**: Adaptive 5-tab layout (Home, Money, Business, Insights, Profile) paired with a central floating action button (+) for rapid transaction logging.
5. **Trilingual Support & PDF Statement Export**: Multi-language support (English, Kinyarwanda, French) and instant exportable PDF financial statements and reports.

## High-Level Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven) and Compose Material Adaptive library
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **Architecture**: MVVM / Unidirectional Data Flow (UDF) with StateFlow and ViewModel

## Implementation Steps
**Total Duration:** 15m 6s

### Task_1_IfarangaBrandingAndTheme: Rebrand app to IFARANGA ('Your money. One place.'), create abstract geometric I/M emblem logo & launcher icon, apply Dark Fintech palette (Deep Charcoal, Emerald, Gold), and add Trilingual support (English, Kinyarwanda, French).
- **Status:** COMPLETED
- **Updates:** Rebranded app to IFARANGA ('Know your money. / Amafaranga yawe. Uyamenye.'), created abstract geometric I/M emblem logo vector ic_ifaranga_logo.xml and app icon, applied Dark Fintech palette (Deep Charcoal #090D16, Primary Emerald #10B981, Accent Gold #EAB308), implemented Trilingual support (English, Kinyarwanda, French EN / RW / FR) across all screens and drawer navigation, updated LanguageManager, LocalStrings, and LanguageManagerTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - IFARANGA branding and abstract geometric I/M emblem logo applied
  - Dark Fintech theme (Deep Charcoal, Emerald, Gold) implemented
  - Trilingual EN / RW / FR switcher working across all screens
  - build pass

### Task_2_IfarangaPulseAndFinancialHealth: Implement Daily Financial Pulse card on Home screen with animated pulse indicator, Financial Health Score (Cash Flow, Debt, Savings, Expenses breakdown), and IFARANGA Intelligence smart insights & 30-day forecast alerts.
- **Status:** COMPLETED
- **Updates:** Implemented FinancialHealthCalculator, FinancialHealthReport, FinancialPulseCard with live animated pulsing indicator, FinancialHealthScoreCard with category breakdown (Cash Flow, Debt, Savings, Expenses, Profitability), IfarangaIntelligenceCard with 30-day forecast and cash shortage alert banners, updated DashboardViewModel and DashboardScreen, created InsightsScreen, and added unit tests FinancialHealthCalculatorTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - Financial Pulse card displays RWF In, Out, Net movement, and live animated pulse
  - Financial Health Score component renders 82/100 status with breakdown
  - IFARANGA Intelligence card displays smart insights and 30-day forecast
  - build pass
- **Duration:** 6m 21s

### Task_3_MoneyFlowAndNavigation5Tabs: Build 5-Tab Bottom Navigation (Home, Money, Business, Insights, Profile), Central Quick Action FAB (+) modal, and Money Flow visualization screen (Money In -> Saved/Spent -> Retained).
- **Status:** COMPLETED
- **Updates:** Implemented 5-Tab Bottom Navigation Bar (Home, Money, Business, Insights, Profile) with central Quick Action FAB (+) opening QuickActionBottomSheet (Income, Expense, Debt, Sale, Transfer, Goal), MoneyFlowScreen & MoneyFlowViewModel with Money In -> Saved / Spent -> Retained visual diagram and category breakdown, BusinessHubScreen sub-tabs, ProfileScreen, and unit tests MoneyFlowViewModelTest. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - 5-Tab Bottom Navigation bar rendered
  - Central Quick Action FAB (+) modal opens options for Income, Expense, Debt, Sale, Transfer, Goal
  - Money Flow screen visualizes cash inflow, spent, and retained breakdown
  - build pass
- **Duration:** 8m 45s

### Task_4_BusinessPOSAndVerification: Integrate Business & POS Lite with barcode scanner, PDF statement exporter, run unit tests, verify assembleDebug build, and push to GitHub.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - POS Lite checkout and barcode scanner working
  - PdfDocument API exports downloadable PDF statements
  - All unit tests pass
  - Codebase committed and pushed to https://github.com/shema-f/ledger.git
- **StartTime:** 2026-09-09 21:43:24 SAST

