package com.bigheadapps.monkee.ui.fragments.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.Functions
import com.bigheadapps.monkee.helpers.Raver
import com.bigheadapps.monkee.models.Order
import com.bigheadapps.monkee.models.Store
import com.bigheadapps.monkee.ui.activities.SettingsActivity
import com.bigheadapps.monkee.ui.viewmodels.APIViewModel
import com.flutterwave.raveandroid.rave_java_commons.Meta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_account.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class AccountFragment : Fragment() {

    //Holds the current Firebase firestore (DB) instance
    private val db = Firebase.firestore

    //Hold the authentication state instance
    private val mAuth = FirebaseAuth.getInstance()

    //Create a store variable for the seller (store) and instantiate to null
    private var store = Store()

    //listener for store state
    private lateinit var listener: ListenerRegistration

    //Viewmodel to interact with Flutterwave API
    private val apiViewModel: APIViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        monitorStoreData()
        findStoreWithSameNumber()
        getTotalSales()
    }

    private fun setupUI() {
        pay_setup_fee_button?.setOnClickListener {
            val billing = Functions.isBillingOkay(requireContext())
            if (billing == null) {
                MaterialDialog(requireContext()).show {
                    title(R.string.missing_details)
                    message(R.string.billing_error_message)
                    positiveButton(R.string.go_to_settings) {
                        startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                    }
                }
            } else {
                val metadata = listOf(Meta("uid", mAuth.currentUser!!.uid))
                Raver.deliverRaveInstance(
                    activity = requireActivity(),
                    amount = 20.0,
                    billing = billing,
                    units = 1,
                    productName = "Store setup fee",
                    metadata = metadata,
                    subAccounts = emptyList()
                ).initialize()
            }

            updateUI()
        }
    }

    /**
     * Get the store information and keep monitoring it for changes from the server
     */
    private fun monitorStoreData() {
        mAuth.currentUser?.uid?.let {
            val docRef = db.collection("stores").document(it)
            listener = docRef.addSnapshotListener { value, error ->
                if (error != null) {
                    //Handle error
                } else if (value != null && value.exists()) {
                    store = value.toObject(Store::class.java) ?: Store()

                    //if the store has paid setup fee but has no subaccount yet on DB, then create one with flutterwave API
                    if (store.subAccountId.isBlank() && store.subAccountRef.isBlank() && store.hasPaidSetupFee) {
                        apiViewModel.createSubAccount(
                            accountNumber = store.accountNumber,
                            businessPhone = store.phone,
                            businessName = store.name,
                            businessEmail = mAuth.currentUser!!.email!!
                        )
                    }
                    updateUI()

                    findStoreWithSameNumber()
                }
            }
        }
    }

    private fun updateUI() {
        account_number?.text = resources.getString(R.string.default_text, store.accountNumber)

        if (store.hasPaidSetupFee) {
            pay_setup_fee_card?.visibility = View.GONE

            if (store.subAccountId.isBlank() || store.subAccountRef.isBlank()) {
                completing_setup_card?.visibility = View.VISIBLE
            } else {
                completing_setup_card?.visibility = View.GONE
            }
        } else {
            pay_setup_fee_card?.visibility = View.VISIBLE
        }
    }

    private fun getTotalSales() {
        mAuth.currentUser?.uid?.let {
            db.collection("orders").whereEqualTo("sellerId", it).get()
                .addOnSuccessListener { data ->
                    val orders = mutableListOf<Order>()
                    var totalEarnings = 0.0
                    var totalDispatcherEarnings = 0.0
                    for (doc in data) {
                        val order = doc.toObject(Order::class.java)
                        orders.add(order)
                        totalEarnings += order.merchantCut
                        totalDispatcherEarnings += order.dispatcherCut
                    }
                    total_sales?.text = orders.size.toString()
                    total_earnings?.text = "$$totalEarnings"
                    total_dispatcher_earnings?.text = "$$totalDispatcherEarnings"
                }.addOnFailureListener {

                }
        }
    }


    /**
     * Subaccount must have different phone numbers so each store but have unique numbers
     * this function check that and alert the user of that case
     */
    private fun findStoreWithSameNumber() {
        db.collection("stores").whereEqualTo("phone", store.phone)
            .addSnapshotListener { value, error ->
                if (error == null && value != null) {
                    if(value.size() > 1){
                        MaterialDialog(requireContext()).show {
                            title(R.string.duplicate_phone_number)
                            message(R.string.phone_number_warning)
                            positiveButton(R.string.okay) {
                                dismiss()
                            }
                        }
                    }
                }
            }
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }
}