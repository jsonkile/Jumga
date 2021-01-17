package com.bigheadapps.monkee.helpers

import android.app.Activity
import com.bigheadapps.monkee.BuildConfig
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.models.Billing
import com.flutterwave.raveandroid.RaveUiManager
import com.flutterwave.raveandroid.rave_java_commons.Meta
import com.flutterwave.raveandroid.rave_java_commons.SubAccount
import java.util.*

/**
 * Static class to initialize Rave based on the configuration
 */

class Raver {
    companion object {

        /**
         * create the rave instance
         * @param activity the calling activity
         * @param productName the name of the product paid for
         * @param units the number of the products paid for
         * @param amount the amount of the order
         * @param billing the billing information of the user
         * @param metadata meta data to pass to rave
         * @param subAccounts list of subaccounts to split payment with
         */


        fun deliverRaveInstance(
            activity: Activity,
            productName: String,
            units: Int,
            amount: Double,
            billing: Billing,
            metadata: List<Meta>,
            subAccounts: List<SubAccount>
        ): RaveUiManager {
            val raveUiManager = RaveUiManager(activity)
                .setAmount(Functions.convertAmount(amount, billing))
                .setEmail(billing.email)
                .setfName(billing.fname)
                .setlName(billing.lname)
                .acceptBarterPayments(true)
                .acceptCardPayments(true)
                .setNarration("Purchase of $units $productName")
                .setPublicKey(BuildConfig.PUBLIC_KEY)
                .setEncryptionKey(BuildConfig.ENCRYPTION_KEY)
                .setTxRef(UUID.randomUUID().toString())
                .setPhoneNumber(billing.phone, false)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .allowSaveCardFeature(true)
                .onStagingEnv(true)
                .setMeta(metadata)
                .withTheme(R.style.RaveTheme)

            if(subAccounts.isNotEmpty()) raveUiManager.setSubAccounts(subAccounts)

            //Depending on the country, configure rave accordingly
            return when (billing.country) {
                "Nigeria" -> {
                    raveUiManager
                        .setCurrency("NGN")
                        .setCountry("NG")
                        .acceptAccountPayments(true)
                        .acceptUssdPayments(true)
                }

                "Ghana" -> {
                    raveUiManager
                        .setCurrency("GHS")
                        .setCountry("GH")
                        .acceptAccountPayments(true)
                        .acceptGHMobileMoneyPayments(true)
                }

                "Kenya" -> {
                    raveUiManager
                        .setCurrency("KES")
                        .setCountry("KE")
                        .acceptAccountPayments(true)
                        .acceptMpesaPayments(true)
                }

                "United Kingdom" -> {
                    raveUiManager
                        .setCurrency("GBP")
                        .setCountry("NG")
                        .acceptCardPayments(true)
                }

                else -> {
                    raveUiManager
                        .setCurrency("NGN")
                        .setCountry("NG")
                        .acceptAccountPayments(true)
                        .acceptUssdPayments(true)
                }
            }
        }
    }
}