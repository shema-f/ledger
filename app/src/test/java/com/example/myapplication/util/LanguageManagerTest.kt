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
    fun testKinyarwandaStringResolutionForRequiredKeys() {
        LanguageManager.setLanguage(AppLanguage.KINYARWANDA)

        // Common
        assertEquals("Imari", LocalStrings.get("app_name"))
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
    fun testSwitchingBackToEnglish() {
        LanguageManager.setLanguage(AppLanguage.KINYARWANDA)
        assertEquals("Icuruzwa rya none", LocalStrings.get("Today's Sales"))

        LanguageManager.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, LanguageManager.currentLanguage.value)
        assertEquals("Today's Sales", LocalStrings.get("Today's Sales"))
        assertEquals("Total Outstanding Debts", LocalStrings.get("Total Outstanding Debts"))
    }
}
