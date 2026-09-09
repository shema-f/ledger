package com.example.myapplication.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

object LocalStrings {

    private val translations = mapOf(
        // Common
        "app_name" to Pair("Imari", "Imari"),
        "App Name" to Pair("Imari", "Imari"),
        "dashboard" to Pair("Dashboard", "Dashboard"),
        "Dashboard" to Pair("Dashboard", "Dashboard"),
        "inventory" to Pair("Inventory & POS", "Ibicuruzwa & POS"),
        "Inventory" to Pair("Inventory & POS", "Ibicuruzwa & POS"),
        "Inventory & POS" to Pair("Inventory & POS", "Ibicuruzwa & POS"),
        "Inventory & POS Lite" to Pair("Inventory & POS Lite", "Ibicuruzwa & POS Lite"),
        "customers" to Pair("Customers", "Abakiriya"),
        "Customers" to Pair("Customers", "Abakiriya"),
        "Customer Ledger & Debts" to Pair("Customer Ledger & Debts", "Igitabo cy'Abakiriya & Amadeni"),
        "loans" to Pair("Loans & Liabilities", "Inguzanyo & Amadeni"),
        "Loans" to Pair("Loans & Liabilities", "Inguzanyo & Amadeni"),
        "Loans & Business Liabilities" to Pair("Loans & Business Liabilities", "Inguzanyo za Banki & Amadeni"),
        "momo_feed" to Pair("MoMo Live Feed", "Urubuga rw'Ibyinjiye"),
        "MoMo Feed" to Pair("MoMo Live Feed", "Urubuga rw'Ibyinjiye"),
        "MoMo & Bank Live Feed" to Pair("MoMo & Bank Live Feed", "Urubuga rw'Ibyinjiye rw'MoMo & Banki"),
        "reports" to Pair("Financial Reports", "Raporu z'Imari"),
        "Reports" to Pair("Financial Reports", "Raporu z'Imari"),
        "Financial Reports & PDF" to Pair("Financial Reports & PDF", "Raporu z'Imari & PDF"),
        "security" to Pair("Security", "Umutekano"),
        "Security" to Pair("Security", "Umutekano"),
        "Security & Permissions" to Pair("Security & Permissions", "Umutekano n'Uburenganzira"),
        "settings" to Pair("Settings", "Igenamiterere"),
        "Settings" to Pair("Settings", "Igenamiterere"),
        "language" to Pair("Language", "Ururimi"),
        "Language" to Pair("Language", "Ururimi"),
        "close" to Pair("Close", "Funga"),
        "Close" to Pair("Close", "Funga"),
        "save" to Pair("Save", "Bika"),
        "Save" to Pair("Save", "Bika"),
        "cancel" to Pair("Cancel", "Hagarika"),
        "Cancel" to Pair("Cancel", "Hagarika"),
        "rwf" to Pair("RWF", "RWF"),
        "RWF" to Pair("RWF", "RWF"),

        // Dashboard
        "todays_sales" to Pair("Today's Sales", "Icuruzwa rya none"),
        "Today's Sales" to Pair("Today's Sales", "Icuruzwa rya none"),
        "total_outstanding_debts" to Pair("Total Outstanding Debts", "Amadeni y'abakiriya yose"),
        "Total Outstanding Debts" to Pair("Total Outstanding Debts", "Amadeni y'abakiriya yose"),
        "active_debtors" to Pair("Active Debtors", "Abakiriya bafite amadeni"),
        "Active Debtors" to Pair("Active Debtors", "Abakiriya bafite amadeni"),
        "add_debt" to Pair("+ Ideni Shya", "+ Ideni Shya"),
        "Add Debt" to Pair("Add Debt", "+ Ideni Shya"),
        "+ Ideni Shya" to Pair("+ Ideni Shya", "+ Ideni Shya"),
        "record_payment" to Pair("Record Payment", "Kwishyura Ideni"),
        "Record Payment" to Pair("Record Payment", "Kwishyura Ideni"),
        "new_cash_sale" to Pair("New Cash Sale", "Icuruzwa rya cash"),
        "New Cash Sale" to Pair("New Cash Sale", "Icuruzwa rya cash"),
        "recent_activity" to Pair("Recent Activity", "Ibyakozwe vuba"),
        "Recent Activity" to Pair("Recent Activity", "Ibyakozwe vuba"),
        "merchant_overview" to Pair("Merchant Overview", "Inshamake y'Ubucuruzi"),
        "Merchant Overview" to Pair("Merchant Overview", "Inshamake y'Ubucuruzi"),
        "quick_actions" to Pair("Quick Actions", "Nihafi"),
        "Quick Actions" to Pair("Quick Actions", "Nihafi"),
        "no_recent_activity" to Pair("No recent activity recorded", "Nta byakozwe vuba birandikwa"),
        "No recent activity recorded" to Pair("No recent activity recorded", "Nta byakozwe vuba birandikwa"),

        // Inventory & POS
        "product_catalog" to Pair("Product Catalog", "Ibicuruzwa byose"),
        "Product Catalog" to Pair("Product Catalog", "Ibicuruzwa byose"),
        "low_stock_alert" to Pair("Low Stock Alert", "Ibicuruzwa biri gushira"),
        "Low Stock Alert" to Pair("Low Stock Alert", "Ibicuruzwa biri gushira"),
        "Low Stock Alerts" to Pair("Low Stock Alerts", "Ibicuruzwa biri gushira"),
        "buying_price" to Pair("Buying Price", "Igiciro waguze"),
        "Buying Price" to Pair("Buying Price", "Igiciro waguze"),
        "selling_price" to Pair("Selling Price", "Igiciro ugurisha"),
        "Selling Price" to Pair("Selling Price", "Igiciro ugurisha"),
        "profit_margin" to Pair("Profit Margin", "Inyungu kuri kimwe"),
        "Profit Margin" to Pair("Profit Margin", "Inyungu kuri kimwe"),
        "pos_checkout" to Pair("POS Checkout", "Gukora Icuruzwa"),
        "POS Checkout" to Pair("POS Checkout", "Gukora Icuruzwa"),
        "select_account" to Pair("Select Account", "Hitamo Konti"),
        "Select Account" to Pair("Select Account", "Hitamo Konti"),
        "add_product" to Pair("Add Product", "Ongeramo Igicuruzwa"),
        "Add Product" to Pair("Add Product", "Ongeramo Igicuruzwa"),
        "stock_quantity" to Pair("Stock Quantity", "Iquantite iri mu stoke"),
        "Stock Quantity" to Pair("Stock Quantity", "Iquantite iri mu stoke"),
        "no_products_found" to Pair("No products found.", "Nta bicuruzwa bibonetse."),
        "No products found." to Pair("No products found.", "Nta bicuruzwa bibonetse."),

        // Customers & Loans
        "customer_debts" to Pair("Customer Debts (Amadeni)", "Amadeni y'abakiriya"),
        "Customer Debts (Amadeni)" to Pair("Customer Debts (Amadeni)", "Amadeni y'abakiriya"),
        "business_loans" to Pair("Business Loans (Inguzanyo)", "Inguzanyo z'ubucuruzi"),
        "Business Loans (Inguzanyo)" to Pair("Business Loans (Inguzanyo)", "Inguzanyo z'ubucuruzi"),
        "debts_owed_to_you" to Pair("Debts Owed to You", "Ubwishyu bw'abakiriya"),
        "Debts Owed to You" to Pair("Debts Owed to You", "Ubwishyu bw'abakiriya"),
        "business_loans_you_owe" to Pair("Business Loans You Owe", "Inguzanyo za banki zituraho"),
        "Business Loans You Owe" to Pair("Business Loans You Owe", "Inguzanyo za banki zituraho"),
        "send_reminder" to Pair("Send Reminder", "Ohereza Urwibutso rwa SMS"),
        "Send Reminder" to Pair("Send Reminder", "Ohereza Urwibutso rwa SMS"),
        "record_repayment" to Pair("Record Repayment", "Kwandika Kwishyura"),
        "Record Repayment" to Pair("Record Repayment", "Kwandika Kwishyura"),
        "add_customer" to Pair("Add Customer", "Ongeramo Umukiriya"),
        "Add Customer" to Pair("Add Customer", "Ongeramo Umukiriya"),
        "customer_name" to Pair("Customer Name", "Izina ry'Umukiriya"),
        "Customer Name" to Pair("Customer Name", "Izina ry'Umukiriya"),
        "phone_number" to Pair("Phone Number", "Nomero ya Telefone"),
        "Phone Number" to Pair("Phone Number", "Nomero ya Telefone"),
        "total_debt" to Pair("Total Debt", "Ideni Ryose"),
        "Total Debt" to Pair("Total Debt", "Ideni Ryose"),
        "loan_provider" to Pair("Loan Provider", "Uwatanze Inguzanyo"),
        "Loan Provider" to Pair("Loan Provider", "Uwatanze Inguzanyo"),
        "principal_amount" to Pair("Principal Amount", "Ayo Wagufe"),
        "Principal Amount" to Pair("Principal Amount", "Ayo Wagufe"),
        "add_loan" to Pair("Add Loan", "Ongeramo Inguzanyo"),
        "Add Loan" to Pair("Add Loan", "Ongeramo Inguzanyo"),

        // MoMo Live Feed
        "incoming_payment_alerts" to Pair("Incoming Payment Alerts", "Urubuga rw'Ibyinjiye"),
        "Incoming Payment Alerts" to Pair("Incoming Payment Alerts", "Urubuga rw'Ibyinjiye"),
        "auto_reconciled" to Pair("Auto-Reconciled", "Byahujwe mu buryo bwikora"),
        "Auto-Reconciled" to Pair("Auto-Reconciled", "Byahujwe mu buryo bwikora"),
        "pending_match" to Pair("Pending Match", "Bitegereje guhuzwa"),
        "Pending Match" to Pair("Pending Match", "Bitegereje guhuzwa"),
        "assign_transaction" to Pair("Assign Transaction", "Guhuza Ibyinjiye"),
        "Assign Transaction" to Pair("Assign Transaction", "Guhuza Ibyinjiye"),
        "payer_name" to Pair("Payer Name", "Izina ry'Uwishyuye"),
        "Payer Name" to Pair("Payer Name", "Izina ry'Uwishyuye"),
        "transaction_ref" to Pair("Transaction Ref", "Inomero y'Icyakozwe"),
        "Transaction Ref" to Pair("Transaction Ref", "Inomero y'Icyakozwe"),
        "amount" to Pair("Amount", "Ayo yishyuye"),
        "Amount" to Pair("Amount", "Ayo yishyuye"),

        // Reports & PDF
        "financial_reports" to Pair("Financial Reports", "Raporu z'Imari"),
        "Financial Reports" to Pair("Financial Reports", "Raporu z'Imari"),
        "gross_revenue" to Pair("Gross Revenue", "Ibyinjiye byose"),
        "Gross Revenue" to Pair("Gross Revenue", "Ibyinjiye byose"),
        "cost_of_goods_sold" to Pair("Cost of Goods Sold", "Igiciro cy'Ibicuruzwa"),
        "Cost of Goods Sold" to Pair("Cost of Goods Sold", "Igiciro cy'Ibicuruzwa"),
        "gross_operating_profit" to Pair("Gross Operating Profit", "Inyungu yose hamwe"),
        "Gross Operating Profit" to Pair("Gross Operating Profit", "Inyungu yose hamwe"),
        "export_pdf_statement" to Pair("Export PDF Statement", "Sohora Raporu muri PDF"),
        "Export PDF Statement" to Pair("Export PDF Statement", "Sohora Raporu muri PDF"),
        "select_date_range" to Pair("Select Date Range", "Hitamo Igihe"),
        "Select Date Range" to Pair("Select Date Range", "Hitamo Igihe"),
        "generating_pdf" to Pair("Generating PDF...", "Gukora PDF..."),
        "Generating PDF..." to Pair("Generating PDF...", "Gukora PDF..."),
        "net_profit" to Pair("Net Profit", "Inyungu Isagamba"),
        "Net Profit" to Pair("Net Profit", "Inyungu Isagamba"),

        // Security & Auth
        "security_and_pin" to Pair("Security & PIN", "Umutekano & PIN"),
        "Security & PIN" to Pair("Security & PIN", "Umutekano & PIN"),
        "app_permissions" to Pair("App Permissions", "Uburenganzira bwa Porogaramu"),
        "App Permissions" to Pair("App Permissions", "Uburenganzira bwa Porogaramu"),
        "app_lock" to Pair("App Lock", "Gufunga Porogaramu"),
        "App Lock" to Pair("App Lock", "Gufunga Porogaramu"),
        "enter_pin" to Pair("Enter PIN", "Andika PIN"),
        "Enter PIN" to Pair("Enter PIN", "Andika PIN"),
        "set_new_pin" to Pair("Set New PIN", "Shyiraho PIN Nshya"),
        "Set New PIN" to Pair("Set New PIN", "Shyiraho PIN Nshya"),
        "confirm_pin" to Pair("Confirm PIN", "Emeza PIN"),
        "Confirm PIN" to Pair("Confirm PIN", "Emeza PIN"),
        "biometric_auth" to Pair("Biometric Auth", "Umutekano wa Urutoki / Isura"),
        "Biometric Auth" to Pair("Biometric Auth", "Umutekano wa Urutoki / Isura"),
        "unlock_imari" to Pair("Unlock Imari", "Fungura Imari"),
        "Unlock Imari" to Pair("Unlock Imari", "Fungura Imari"),
        "lock_app" to Pair("Lock App", "Funga Porogaramu"),
        "Lock App" to Pair("Lock App", "Funga Porogaramu"),

        // Navigation & Misc
        "smart_financial_ledger" to Pair("Smart Financial Ledger", "Igitabo cy'Imari cy'Ubwenge"),
        "Smart Financial Ledger" to Pair("Smart Financial Ledger", "Igitabo cy'Imari cy'Ubwenge"),
        "light" to Pair("Light", "Urumuri"),
        "Light" to Pair("Light", "Urumuri"),
        "dark" to Pair("Dark", "Umwijima"),
        "Dark" to Pair("Dark", "Umwijima"),
        "customer_detail" to Pair("Customer Detail", "Ibirambuye ku Mukiriya"),
        "Customer Detail" to Pair("Customer Detail", "Ibirambuye ku Mukiriya"),
        "back" to Pair("Back", "Subira Inyuma"),
        "Back" to Pair("Back", "Subira Inyuma"),
        "english" to Pair("English", "English"),
        "English" to Pair("English", "English"),
        "kinyarwanda" to Pair("Kinyarwanda", "Kinyarwanda"),
        "Kinyarwanda" to Pair("Kinyarwanda", "Kinyarwanda")
    )

    fun get(key: String, language: AppLanguage = LanguageManager.currentLanguage.value): String {
        val pair = translations[key] ?: translations.entries.find { it.key.equals(key, ignoreCase = true) }?.value
        if (pair != null) {
            return when (language) {
                AppLanguage.ENGLISH -> pair.first
                AppLanguage.KINYARWANDA -> pair.second
            }
        }
        return key
    }
}

@Composable
fun localizedString(key: String): String {
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    return LocalStrings.get(key, currentLanguage)
}
