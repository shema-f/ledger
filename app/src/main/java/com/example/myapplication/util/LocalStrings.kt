package com.example.myapplication.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

object LocalStrings {

    // Map storing Triple(English, Kinyarwanda, French)
    private val translations = mapOf(
        // Common & App Identity
        "app_name" to Triple("IFARANGA", "IFARANGA", "IFARANGA"),
        "App Name" to Triple("IFARANGA", "IFARANGA", "IFARANGA"),
        "dashboard" to Triple("Dashboard", "Dashboard", "Tableau de bord"),
        "Dashboard" to Triple("Dashboard", "Dashboard", "Tableau de bord"),
        "inventory" to Triple("Inventory & POS", "Ibicuruzwa & POS", "Inventaire et POS"),
        "Inventory" to Triple("Inventory & POS", "Ibicuruzwa & POS", "Inventaire et POS"),
        "Inventory & POS" to Triple("Inventory & POS", "Ibicuruzwa & POS", "Inventaire et POS"),
        "Inventory & POS Lite" to Triple("Inventory & POS Lite", "Ibicuruzwa & POS Lite", "Inventaire et POS Lite"),
        "customers" to Triple("Customers", "Abakiriya", "Clients"),
        "Customers" to Triple("Customers", "Abakiriya", "Clients"),
        "Customer Ledger & Debts" to Triple("Customer Ledger & Debts", "Igitabo cy'Abakiriya & Amadeni", "Livre de compte & Dettes clients"),
        "loans" to Triple("Loans & Liabilities", "Inguzanyo & Amadeni", "Emprunts et Passifs"),
        "Loans" to Triple("Loans & Liabilities", "Inguzanyo & Amadeni", "Emprunts et Passifs"),
        "Loans & Business Liabilities" to Triple("Loans & Business Liabilities", "Inguzanyo za Banki & Amadeni", "Emprunts et Passifs commerciaux"),
        "momo_feed" to Triple("MoMo Live Feed", "Urubuga rw'Ibyinjiye", "Flux MoMo en direct"),
        "MoMo Feed" to Triple("MoMo Live Feed", "Urubuga rw'Ibyinjiye", "Flux MoMo en direct"),
        "MoMo & Bank Live Feed" to Triple("MoMo & Bank Live Feed", "Urubuga rw'Ibyinjiye rw'MoMo & Banki", "Flux MoMo et Banque en direct"),
        "reports" to Triple("Financial Reports", "Raporu z'Imari", "Rapports financiers"),
        "Reports" to Triple("Financial Reports", "Raporu z'Imari", "Rapports financiers"),
        "Financial Reports & PDF" to Triple("Financial Reports & PDF", "Raporu z'Imari & PDF", "Rapports financiers et PDF"),
        "security" to Triple("Security", "Umutekano", "Sécurité"),
        "Security" to Triple("Security", "Umutekano", "Sécurité"),
        "Security & Permissions" to Triple("Security & Permissions", "Umutekano n'Uburenganzira", "Sécurité et Autorisations"),
        "settings" to Triple("Settings", "Igenamiterere", "Paramètres"),
        "Settings" to Triple("Settings", "Igenamiterere", "Paramètres"),
        "language" to Triple("Language", "Ururimi", "Langue"),
        "Language" to Triple("Language", "Ururimi", "Langue"),
        "close" to Triple("Close", "Funga", "Fermer"),
        "Close" to Triple("Close", "Funga", "Fermer"),
        "save" to Triple("Save", "Bika", "Enregistrer"),
        "Save" to Triple("Save", "Bika", "Enregistrer"),
        "cancel" to Triple("Cancel", "Hagarika", "Annuler"),
        "Cancel" to Triple("Cancel", "Hagarika", "Annuler"),
        "rwf" to Triple("RWF", "RWF", "RWF"),
        "RWF" to Triple("RWF", "RWF", "RWF"),

        // Dashboard
        "todays_sales" to Triple("Today's Sales", "Icuruzwa rya none", "Ventes d'aujourd'hui"),
        "Today's Sales" to Triple("Today's Sales", "Icuruzwa rya none", "Ventes d'aujourd'hui"),
        "total_outstanding_debts" to Triple("Total Outstanding Debts", "Amadeni y'abakiriya yose", "Total des dettes en cours"),
        "Total Outstanding Debts" to Triple("Total Outstanding Debts", "Amadeni y'abakiriya yose", "Total des dettes en cours"),
        "active_debtors" to Triple("Active Debtors", "Abakiriya bafite amadeni", "Débiteurs actifs"),
        "Active Debtors" to Triple("Active Debtors", "Abakiriya bafite amadeni", "Débiteurs actifs"),
        "add_debt" to Triple("+ New Debt", "+ Ideni Shya", "+ Nouvelle dette"),
        "Add Debt" to Triple("Add Debt", "+ Ideni Shya", "+ Nouvelle dette"),
        "+ Ideni Shya" to Triple("+ New Debt", "+ Ideni Shya", "+ Nouvelle dette"),
        "record_payment" to Triple("Record Payment", "Kwishyura Ideni", "Enregistrer un paiement"),
        "Record Payment" to Triple("Record Payment", "Kwishyura Ideni", "Enregistrer un paiement"),
        "new_cash_sale" to Triple("New Cash Sale", "Icuruzwa rya cash", "Nouvelle vente au comptant"),
        "New Cash Sale" to Triple("New Cash Sale", "Icuruzwa rya cash", "Nouvelle vente au comptant"),
        "recent_activity" to Triple("Recent Activity", "Ibyakozwe vuba", "Activité récente"),
        "Recent Activity" to Triple("Recent Activity", "Ibyakozwe vuba", "Activité récente"),
        "merchant_overview" to Triple("Merchant Overview", "Inshamake y'Ubucuruzi", "Aperçu commerçant"),
        "Merchant Overview" to Triple("Merchant Overview", "Inshamake y'Ubucuruzi", "Aperçu commerçant"),
        "quick_actions" to Triple("Quick Actions", "Nihafi", "Actions rapides"),
        "Quick Actions" to Triple("Quick Actions", "Nihafi", "Actions rapides"),
        "no_recent_activity" to Triple("No recent activity recorded", "Nta byakozwe vuba birandikwa", "Aucune activité récente enregistrée"),
        "No recent activity recorded" to Triple("No recent activity recorded", "Nta byakozwe vuba birandikwa", "Aucune activité récente enregistrée"),

        // Inventory & POS
        "product_catalog" to Triple("Product Catalog", "Ibicuruzwa byose", "Catalogue de produits"),
        "Product Catalog" to Triple("Product Catalog", "Ibicuruzwa byose", "Catalogue de produits"),
        "low_stock_alert" to Triple("Low Stock Alert", "Ibicuruzwa biri gushira", "Alerte stock faible"),
        "Low Stock Alert" to Triple("Low Stock Alert", "Ibicuruzwa biri gushira", "Alerte stock faible"),
        "Low Stock Alerts" to Triple("Low Stock Alerts", "Ibicuruzwa biri gushira", "Alertes stock faible"),
        "buying_price" to Triple("Buying Price", "Igiciro waguze", "Prix d'achat"),
        "Buying Price" to Triple("Buying Price", "Igiciro waguze", "Prix d'achat"),
        "selling_price" to Triple("Selling Price", "Igiciro ugurisha", "Prix de vente"),
        "Selling Price" to Triple("Selling Price", "Igiciro ugurisha", "Prix de vente"),
        "profit_margin" to Triple("Profit Margin", "Inyungu kuri kimwe", "Marge bénéficiaire"),
        "Profit Margin" to Triple("Profit Margin", "Inyungu kuri kimwe", "Marge bénéficiaire"),
        "pos_checkout" to Triple("POS Checkout", "Gukora Icuruzwa", "Caisse POS"),
        "POS Checkout" to Triple("POS Checkout", "Gukora Icuruzwa", "Caisse POS"),
        "select_account" to Triple("Select Account", "Hitamo Konti", "Sélectionner un compte"),
        "Select Account" to Triple("Select Account", "Hitamo Konti", "Sélectionner un compte"),
        "add_product" to Triple("Add Product", "Ongeramo Igicuruzwa", "Ajouter un produit"),
        "Add Product" to Triple("Add Product", "Ongeramo Igicuruzwa", "Ajouter un produit"),
        "stock_quantity" to Triple("Stock Quantity", "Iquantite iri mu stoke", "Quantité en stock"),
        "Stock Quantity" to Triple("Stock Quantity", "Iquantite iri mu stoke", "Quantité en stock"),
        "no_products_found" to Triple("No products found.", "Nta bicuruzwa bibonetse.", "Aucun produit trouvé."),
        "No products found." to Triple("No products found.", "Nta bicuruzwa bibonetse.", "Aucun produit trouvé."),
        "search_products" to Triple("Search products...", "Shakisha ibicuruzwa...", "Rechercher des produits..."),

        // Customers & Loans
        "customer_debts" to Triple("Customer Debts", "Amadeni y'abakiriya", "Dettes clients"),
        "Customer Debts (Amadeni)" to Triple("Customer Debts (Amadeni)", "Amadeni y'abakiriya", "Dettes clients"),
        "business_loans" to Triple("Business Loans", "Inguzanyo z'ubucuruzi", "Emprunts commerciaux"),
        "Business Loans (Inguzanyo)" to Triple("Business Loans (Inguzanyo)", "Inguzanyo z'ubucuruzi", "Emprunts commerciaux"),
        "debts_owed_to_you" to Triple("Debts Owed to You", "Ubwishyu bw'abakiriya", "Créances clients"),
        "Debts Owed to You" to Triple("Debts Owed to You", "Ubwishyu bw'abakiriya", "Créances clients"),
        "business_loans_you_owe" to Triple("Business Loans You Owe", "Inguzanyo za banki zituraho", "Emprunts bancaires dus"),
        "Business Loans You Owe" to Triple("Business Loans You Owe", "Inguzanyo za banki zituraho", "Emprunts bancaires dus"),
        "send_reminder" to Triple("Send SMS Reminder", "Ohereza Urwibutso rwa SMS", "Envoyer un rappel SMS"),
        "Send Reminder" to Triple("Send Reminder", "Ohereza Urwibutso rwa SMS", "Envoyer un rappel SMS"),
        "record_repayment" to Triple("Record Repayment", "Kwandika Kwishyura", "Enregistrer un remboursement"),
        "Record Repayment" to Triple("Record Repayment", "Kwandika Kwishyura", "Enregistrer un remboursement"),
        "add_customer" to Triple("Add Customer", "Ongeramo Umukiriya", "Ajouter un client"),
        "Add Customer" to Triple("Add Customer", "Ongeramo Umukiriya", "Ajouter un client"),
        "customer_name" to Triple("Customer Name", "Izina ry'Umukiriya", "Nom du client"),
        "Customer Name" to Triple("Customer Name", "Izina ry'Umukiriya", "Nom du client"),
        "phone_number" to Triple("Phone Number", "Nomero ya Telefone", "Numéro de téléphone"),
        "Phone Number" to Triple("Phone Number", "Nomero ya Telefone", "Numéro de téléphone"),
        "total_debt" to Triple("Total Debt", "Ideni Ryose", "Dette totale"),
        "Total Debt" to Triple("Total Debt", "Ideni Ryose", "Dette totale"),
        "loan_provider" to Triple("Loan Provider", "Uwatanze Inguzanyo", "Prestataire de prêt"),
        "Loan Provider" to Triple("Loan Provider", "Uwatanze Inguzanyo", "Prestataire de prêt"),
        "principal_amount" to Triple("Principal Amount", "Ayo Wagufe", "Montant principal"),
        "Principal Amount" to Triple("Principal Amount", "Ayo Wagufe", "Montant principal"),
        "add_loan" to Triple("Add Loan", "Ongeramo Inguzanyo", "Ajouter un prêt"),
        "Add Loan" to Triple("Add Loan", "Ongeramo Inguzanyo", "Ajouter un prêt"),

        // MoMo Live Feed
        "incoming_payment_alerts" to Triple("Incoming Payment Alerts", "Urubuga rw'Ibyinjiye", "Alertes de paiements entrants"),
        "Incoming Payment Alerts" to Triple("Incoming Payment Alerts", "Urubuga rw'Ibyinjiye", "Alertes de paiements entrants"),
        "auto_reconciled" to Triple("Auto-Reconciled", "Byahujwe mu buryo bwikora", "Rapprochement automatique"),
        "Auto-Reconciled" to Triple("Auto-Reconciled", "Byahujwe mu buryo bwikora", "Rapprochement automatique"),
        "pending_match" to Triple("Pending Match", "Bitegereje guhuzwa", "En attente de correspondance"),
        "Pending Match" to Triple("Pending Match", "Bitegereje guhuzwa", "En attente de correspondance"),
        "assign_transaction" to Triple("Assign Transaction", "Guhuza Ibyinjiye", "Assigner la transaction"),
        "Assign Transaction" to Triple("Assign Transaction", "Guhuza Ibyinjiye", "Assigner la transaction"),
        "payer_name" to Triple("Payer Name", "Izina ry'Uwishyuye", "Nom du payeur"),
        "Payer Name" to Triple("Payer Name", "Izina ry'Uwishyuye", "Nom du payeur"),
        "transaction_ref" to Triple("Transaction Ref", "Inomero y'Icyakozwe", "Réf. de la transaction"),
        "Transaction Ref" to Triple("Transaction Ref", "Inomero y'Icyakozwe", "Réf. de la transaction"),
        "amount" to Triple("Amount", "Ayo yishyuye", "Montant"),
        "Amount" to Triple("Amount", "Ayo yishyuye", "Montant"),

        // Reports & PDF
        "financial_reports" to Triple("Financial Reports", "Raporu z'Imari", "Rapports financiers"),
        "Financial Reports" to Triple("Financial Reports", "Raporu z'Imari", "Rapports financiers"),
        "gross_revenue" to Triple("Gross Revenue", "Ibyinjiye byose", "Chiffre d'affaires brut"),
        "Gross Revenue" to Triple("Gross Revenue", "Ibyinjiye byose", "Chiffre d'affaires brut"),
        "cost_of_goods_sold" to Triple("Cost of Goods Sold", "Igiciro cy'Ibicuruzwa", "Coût des marchandises vendues"),
        "Cost of Goods Sold" to Triple("Cost of Goods Sold", "Igiciro cy'Ibicuruzwa", "Coût des marchandises vendues"),
        "gross_operating_profit" to Triple("Gross Operating Profit", "Inyungu yose hamwe", "Bénéfice d'exploitation brut"),
        "Gross Operating Profit" to Triple("Gross Operating Profit", "Inyungu yose hamwe", "Bénéfice d'exploitation brut"),
        "export_pdf_statement" to Triple("Export PDF Statement", "Sohora Raporu muri PDF", "Exporter le relevé PDF"),
        "Export PDF Statement" to Triple("Export PDF Statement", "Sohora Raporu muri PDF", "Exporter le relevé PDF"),
        "select_date_range" to Triple("Select Date Range", "Hitamo Igihe", "Sélectionner la période"),
        "Select Date Range" to Triple("Select Date Range", "Hitamo Igihe", "Sélectionner la période"),
        "generating_pdf" to Triple("Generating PDF...", "Gukora PDF...", "Génération du PDF..."),
        "Generating PDF..." to Triple("Generating PDF...", "Gukora PDF...", "Génération du PDF..."),
        "net_profit" to Triple("Net Profit", "Inyungu Isagamba", "Bénéfice net"),
        "Net Profit" to Triple("Net Profit", "Inyungu Isagamba", "Bénéfice net"),

        // Security & Auth & Profile
        "security_and_pin" to Triple("Security & PIN", "Umutekano & PIN", "Sécurité & Code PIN"),
        "Security & PIN" to Triple("Security & PIN", "Umutekano & PIN", "Sécurité & Code PIN"),
        "app_permissions" to Triple("App Permissions", "Uburenganzira bwa Porogaramu", "Autorisations de l'application"),
        "App Permissions" to Triple("App Permissions", "Uburenganzira bwa Porogaramu", "Autorisations de l'application"),
        "app_lock" to Triple("App Lock", "Gufunga Porogaramu", "Verrouillage de l'application"),
        "App Lock" to Triple("App Lock", "Gufunga Porogaramu", "Verrouillage de l'application"),
        "enter_pin" to Triple("Enter PIN", "Andika PIN", "Saisir le PIN"),
        "Enter PIN" to Triple("Enter PIN", "Andika PIN", "Saisir le PIN"),
        "set_new_pin" to Triple("Set New PIN", "Shyiraho PIN Nshya", "Définir un nouveau PIN"),
        "Set New PIN" to Triple("Set New PIN", "Shyiraho PIN Nshya", "Définir un nouveau PIN"),
        "confirm_pin" to Triple("Confirm PIN", "Emeza PIN", "Confirmer le PIN"),
        "Confirm PIN" to Triple("Confirm PIN", "Emeza PIN", "Confirmer le PIN"),
        "biometric_auth" to Triple("Biometric Auth", "Umutekano wa Urutoki / Isura", "Authentification biométrique"),
        "Biometric Auth" to Triple("Biometric Auth", "Umutekano wa Urutoki / Isura", "Authentification biométrique"),
        "unlock_ifaranga" to Triple("Unlock IFARANGA", "Fungura IFARANGA", "Déverrouiller IFARANGA"),
        "unlock_imari" to Triple("Unlock IFARANGA", "Fungura IFARANGA", "Déverrouiller IFARANGA"),
        "Unlock Imari" to Triple("Unlock IFARANGA", "Fungura IFARANGA", "Déverrouiller IFARANGA"),
        "lock_app" to Triple("Lock App", "Funga Porogaramu", "Verrouiller"),
        "Lock App" to Triple("Lock App", "Funga Porogaramu", "Verrouiller"),
        "profile" to Triple("Merchant Profile", "Ikurikiranwa ry'ubucuruzi", "Profil Commerçant"),
        "Profile" to Triple("Merchant Profile", "Ikurikiranwa ry'ubucuruzi", "Profil Commerçant"),
        "merchant_profile" to Triple("Merchant Profile", "Ikurikiranwa ry'ubucuruzi", "Profil Commerçant"),

        // Navigation & Languages & Tagline
        "smart_financial_ledger" to Triple("Know your money.", "Amafaranga yawe. Uyamenye.", "Connaissez votre argent."),
        "Smart Financial Ledger" to Triple("Know your money.", "Amafaranga yawe. Uyamenye.", "Connaissez votre argent."),
        "light" to Triple("Light", "Urumuri", "Clair"),
        "Light" to Triple("Light", "Urumuri", "Clair"),
        "dark" to Triple("Dark", "Umwijima", "Sombre"),
        "Dark" to Triple("Dark", "Umwijima", "Sombre"),
        "customer_detail" to Triple("Customer Detail", "Ibirambuye ku Mukiriya", "Détails du client"),
        "Customer Detail" to Triple("Customer Detail", "Ibirambuye ku Mukiriya", "Détails du client"),
        "back" to Triple("Back", "Subira Inyuma", "Retour"),
        "Back" to Triple("Back", "Subira Inyuma", "Retour"),
        "english" to Triple("English", "English", "Anglais"),
        "English" to Triple("English", "English", "Anglais"),
        "kinyarwanda" to Triple("Kinyarwanda", "Kinyarwanda", "Kinyarwanda"),
        "Kinyarwanda" to Triple("Kinyarwanda", "Kinyarwanda", "Kinyarwanda"),
        "french" to Triple("Français", "Français", "Français"),
        "French" to Triple("Français", "Français", "Français"),
        "Français" to Triple("Français", "Français", "Français")
    )

    fun get(key: String, language: AppLanguage = LanguageManager.currentLanguage.value): String {
        val triple = translations[key] ?: translations.entries.find { it.key.equals(key, ignoreCase = true) }?.value
        if (triple != null) {
            return when (language) {
                AppLanguage.ENGLISH -> triple.first
                AppLanguage.KINYARWANDA -> triple.second
                AppLanguage.FRENCH -> triple.third
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
