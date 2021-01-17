package com.bigheadapps.monkee.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.bigheadapps.monkee.models.Billing

class Functions {
    companion object {
        fun dollafy(price: Int): String {
            return "$$price"
        }

        fun convertAmount(amount: Double, billing: Billing): Double {
            return when (billing.country) {
                "United Kingdom" -> amount * 0.74
                "Ghana" -> amount * 5.89
                "Kenya" -> amount * 109.55
                else -> amount * 381.25
            }
        }

        fun getMerchantCut(total: Double): Double {
            val totalAmountMinusDelivery = total - DELIVERY_FEE
            return totalAmountMinusDelivery - (totalAmountMinusDelivery * 0.025)
        }

        fun getDispatcherCut(): Double {
            return DELIVERY_FEE - (DELIVERY_FEE * 0.2)
        }

        fun saveToSharedPreference(key: String, value: String, context: Context) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value)
                .apply()
        }

        fun getFromSharedPreference(key: String, context: Context): String? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(key, null)
        }

        fun getBooleanFromSharedPreference(key: String, context: Context): Boolean {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getBoolean(key, false)
        }

        fun isBillingOkay(context: Context): Billing? {
            val email = getFromSharedPreference("billing-email", context)
            val phone = getFromSharedPreference("billing-phone", context)
            val address = getFromSharedPreference("billing-address", context)
            val fName = getFromSharedPreference("billing-fname", context)
            val lName = getFromSharedPreference("billing-lname", context)
            val country = getFromSharedPreference("billing-country", context)

            return if ((email.isNullOrBlank() || fName.isNullOrBlank() || lName.isNullOrBlank() || address.isNullOrBlank() || phone.isNullOrBlank() || country.isNullOrBlank())) {
                null
            } else {
                Billing(fName, lName, phone, address, email, country)
            }
        }

        fun clearSharedPreference(context: Context) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
        }
    }
}