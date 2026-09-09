package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LanguageManagerTest {

    @Before
    fun setUp() {
        LanguageManager.resetForTest(AppLanguage.ENGLISH)
    }

    @Test
    fun testInitialLanguageIsEnglish() {
        assertEquals(AppLanguage.ENGLISH, LanguageManager.currentLanguage.value)
    }

    @Test
    fun testSetLanguageToKinyarwanda() {
        LanguageManager.setLanguage(AppLanguage.KINYARWANDA)
        assertEquals(AppLanguage.KINYARWANDA, LanguageManager.currentLanguage.value)
    }

    @Test
    fun testSetLanguageToFrench() {
        LanguageManager.setLanguage(AppLanguage.FRENCH)
        assertEquals(AppLanguage.FRENCH, LanguageManager.currentLanguage.value)
    }

    @Test
    fun testKinyarwandaStringResolutionForRequiredKeys() {
        LanguageManager.setLanguage(AppLanguage.KINYARWANDA)

        // Common
        assertEquals("IFARANGA", LocalStrings.get("app_name"))
        assertEquals("Ibicuruzwa & POS", LocalStrings.get("inventory"))
        assertEquals("Abakiriya", LocalStrings.get("customers"))
        assertEquals("Raporu z'Imari", LocalStrings.get("reports"))
        assertEquals("Ururimi", LocalStrings.get("language"))

        // Dashboard
        assertEquals("Icuruzwa rya none", LocalStrings.get("Today's Sales"))
        assertEquals("Amadeni y'abakiriya yose", LocalStrings.get("Total Outstanding Debts"))
        assertEquals("Abakiriya bafite amadeni", LocalStrings.get("Active Debtors"))
        assertEquals("+ Ideni Shya", LocalStrings.get("Add Debt"))
        assertEquals("Kwishyura Ideni", LocalStrings.get("Record Payment"))
        assertEquals("Icuruzwa rya cash", LocalStrings.get("New Cash Sale"))
        assertEquals("Ibyakozwe vuba", LocalStrings.get("Recent Activity"))

        // Inventory & POS
        assertEquals("Ibicuruzwa byose", LocalStrings.get("Product Catalog"))
        assertEquals("Ibicuruzwa biri gushira", LocalStrings.get("Low Stock Alert"))
        assertEquals("Igiciro waguze", LocalStrings.get("Buying Price"))
        assertEquals("Igiciro ugurisha", LocalStrings.get("Selling Price"))
        assertEquals("Inyungu kuri kimwe", LocalStrings.get("Profit Margin"))
        assertEquals("Gukora Icuruzwa", LocalStrings.get("POS Checkout"))
        assertEquals("Hitamo Konti", LocalStrings.get("Select Account"))

        // Customers & Loans
        assertEquals("Amadeni y'abakiriya", LocalStrings.get("Customer Debts (Amadeni)"))
        assertEquals("Inguzanyo z'ubucuruzi", LocalStrings.get("Business Loans (Inguzanyo)"))
        assertEquals("Ubwishyu bw'abakiriya", LocalStrings.get("Debts Owed to You"))
        assertEquals("Inguzanyo za banki zituraho", LocalStrings.get("Business Loans You Owe"))
        assertEquals("Ohereza Urwibutso rwa SMS", LocalStrings.get("Send Reminder"))
        assertEquals("Kwandika Kwishyura", LocalStrings.get("Record Repayment"))

        // MoMo Live Feed
        assertEquals("Urubuga rw'Ibyinjiye", LocalStrings.get("Incoming Payment Alerts"))
        assertEquals("Byahujwe mu buryo bwikora", LocalStrings.get("Auto-Reconciled"))
        assertEquals("Bitegereje guhuzwa", LocalStrings.get("Pending Match"))
        assertEquals("Guhuza Ibyinjiye", LocalStrings.get("Assign Transaction"))

        // Reports & PDF
        assertEquals("Raporu z'Imari", LocalStrings.get("Financial Reports"))
        assertEquals("Ibyinjiye byose", LocalStrings.get("Gross Revenue"))
        assertEquals("Igiciro cy'Ibicuruzwa", LocalStrings.get("Cost of Goods Sold"))
        assertEquals("Inyungu yose hamwe", LocalStrings.get("Gross Operating Profit"))
        assertEquals("Sohora Raporu muri PDF", LocalStrings.get("Export PDF Statement"))
    }

    @Test
    fun testFrenchStringResolutionForRequiredKeys() {
        LanguageManager.setLanguage(AppLanguage.FRENCH)

        // Common
        assertEquals("IFARANGA", LocalStrings.get("app_name"))
        assertEquals("Inventaire et POS", LocalStrings.get("inventory"))
        assertEquals("Clients", LocalStrings.get("customers"))
        assertEquals("Rapports financiers", LocalStrings.get("reports"))
        assertEquals("Langue", LocalStrings.get("language"))

        // Dashboard
        assertEquals("Ventes d'aujourd'hui", LocalStrings.get("Today's Sales"))
        assertEquals("Total des dettes en cours", LocalStrings.get("Total Outstanding Debts"))
        assertEquals("Débiteurs actifs", LocalStrings.get("Active Debtors"))
        assertEquals("+ Nouvelle dette", LocalStrings.get("Add Debt"))
        assertEquals("Enregistrer un paiement", LocalStrings.get("Record Payment"))
        assertEquals("Nouvelle vente au comptant", LocalStrings.get("New Cash Sale"))
        assertEquals("Activité récente", LocalStrings.get("Recent Activity"))

        // Inventory & POS
        assertEquals("Catalogue de produits", LocalStrings.get("Product Catalog"))
        assertEquals("Alerte stock faible", LocalStrings.get("Low Stock Alert"))
        assertEquals("Prix d'achat", LocalStrings.get("Buying Price"))
        assertEquals("Prix de vente", LocalStrings.get("Selling Price"))
        assertEquals("Marge bénéficiaire", LocalStrings.get("Profit Margin"))
        assertEquals("Caisse POS", LocalStrings.get("POS Checkout"))
        assertEquals("Sélectionner un compte", LocalStrings.get("Select Account"))

        // Customers & Loans
        assertEquals("Dettes clients", LocalStrings.get("Customer Debts (Amadeni)"))
        assertEquals("Emprunts commerciaux", LocalStrings.get("Business Loans (Inguzanyo)"))
        assertEquals("Créances clients", LocalStrings.get("Debts Owed to You"))
        assertEquals("Emprunts bancaires dus", LocalStrings.get("Business Loans You Owe"))
        assertEquals("Envoyer un rappel SMS", LocalStrings.get("Send Reminder"))
        assertEquals("Enregistrer un remboursement", LocalStrings.get("Record Repayment"))

        // MoMo Live Feed
        assertEquals("Alertes de paiements entrants", LocalStrings.get("Incoming Payment Alerts"))
        assertEquals("Rapprochement automatique", LocalStrings.get("Auto-Reconciled"))
        assertEquals("En attente de correspondance", LocalStrings.get("Pending Match"))
        assertEquals("Assigner la transaction", LocalStrings.get("Assign Transaction"))

        // Reports & PDF
        assertEquals("Rapports financiers", LocalStrings.get("Financial Reports"))
        assertEquals("Chiffre d'affaires brut", LocalStrings.get("Gross Revenue"))
        assertEquals("Coût des marchandises vendues", LocalStrings.get("Cost of Goods Sold"))
        assertEquals("Bénéfice d'exploitation brut", LocalStrings.get("Gross Operating Profit"))
        assertEquals("Exporter le relevé PDF", LocalStrings.get("Export PDF Statement"))
    }

    @Test
    fun testSwitchingBetweenLanguages() {
        LanguageManager.setLanguage(AppLanguage.KINYARWANDA)
        assertEquals("Icuruzwa rya none", LocalStrings.get("Today's Sales"))

        LanguageManager.setLanguage(AppLanguage.FRENCH)
        assertEquals("Ventes d'aujourd'hui", LocalStrings.get("Today's Sales"))

        LanguageManager.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, LanguageManager.currentLanguage.value)
        assertEquals("Today's Sales", LocalStrings.get("Today's Sales"))
        assertEquals("Total Outstanding Debts", LocalStrings.get("Total Outstanding Debts"))
    }
}
