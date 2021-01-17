package com.bigheadapps.monkee.ui.fragments.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.vvalidator.form
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.problemToast
import com.bigheadapps.monkee.helpers.shortToast
import com.bigheadapps.monkee.models.Store
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.store_update_form.*
import timber.log.Timber.d

class ManageFragment : Fragment() {

    private val db = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()
    private var store = Store()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        monitorStoreData()

        //validate update store form input
        form {
            input(R.id.update_store_name_input) {
                isNotEmpty()
                length().atLeast(3)
            }

            input(R.id.update_store_phone_input) {
                isNotEmpty()
                isNumber()
                length().exactly(11)
            }

            input(R.id.update_store_address_input) {
                isNotEmpty()
                length().atLeast(15)
            }

            input(R.id.update_store_description_input) {
                isNotEmpty()
                length().atLeast(10)
            }

            submitWith(R.id.update_store_button) {
                updateStore(
                    update_store_name_input?.text.toString(),
                    update_store_phone_input?.text.toString(),
                    update_store_address_input?.text.toString(),
                    update_store_description_input?.text.toString()
                )
            }
        }
    }

    /**
     * Get the store information and keep monitoring it for changes from the server
     */
    private fun monitorStoreData() {
        mAuth.currentUser?.uid?.let {
            val docRef = db.collection("stores").document(it)
            docRef.addSnapshotListener { value, error ->
                if (error != null) {
                    //Handle error
                    activity?.problemToast()
                } else if (value != null && value.exists()) {
                    store = value.toObject(Store::class.java) ?: Store()
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        update_store_name_input?.setText(store.name)
        update_store_phone_input?.setText(store.phone)
        update_store_address_input?.setText(store.address)
        update_store_description_input?.setText(store.description)
    }

    /**
     * Send store update data to server
     */
    private fun updateStore(name: String, phone: String, address: String, description: String) {
        val store = hashMapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "description" to description
        )

        mAuth.currentUser?.uid?.let {
            db.collection("stores").document(it)
                .update(store.toMap())
                .addOnSuccessListener {
                    activity?.shortToast("Successful")
                }
                .addOnFailureListener { e ->
                    activity?.problemToast()
                    d("Failed to update: ${e.message.toString()}")
                }
        }
    }
}