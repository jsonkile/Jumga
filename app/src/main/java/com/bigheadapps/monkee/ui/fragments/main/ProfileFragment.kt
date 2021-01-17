package com.bigheadapps.monkee.ui.fragments.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.Functions
import com.bigheadapps.monkee.models.Store
import com.bigheadapps.monkee.ui.activities.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.profile_options.*

class ProfileFragment : Fragment() {

    private val db = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()
    private var store: Store? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        monitorLoginState()
    }

    private fun setupUI() {
        requireActivity().findViewById<MaterialToolbar>(R.id.main_toolbar).title =
            mAuth.currentUser?.email ?: "Profile"

        seller_account_button?.setOnClickListener {
            if (mAuth.currentUser?.uid == null) {
                startActivity(Intent(this.requireContext(), AuthActivity::class.java))
            } else {
                if (store != null) {
                    startActivity(Intent(this.requireContext(), StoreActivity::class.java))
                } else {
                    startActivity(
                        Intent(
                            this.requireContext(),
                            CreateStoreActivity::class.java
                        )
                    )
                }
            }
        }

        my_orders_button?.setOnClickListener {
            if (mAuth.currentUser?.uid == null) {
                startActivity(Intent(this.requireContext(), AuthActivity::class.java))
            } else {
                startActivity(Intent(this.requireContext(), OrdersActivity::class.java))
            }
        }

        settings_button?.setOnClickListener {
            startActivity(Intent(this.requireContext(), SettingsActivity::class.java))
        }

        logout_button?.setOnClickListener {
            mAuth.signOut()
            Functions.clearSharedPreference(requireContext())
            this.findNavController().navigateUp()
        }

        about_button?.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.app_name)
                message(R.string.about_app_speech)
                positiveButton(R.string.nice) {
                    dismiss()
                }
            }
        }
    }

    private fun monitorLoginState() {
        mAuth.addAuthStateListener {
            if (it.currentUser != null) {
                logout_button?.visibility = View.VISIBLE
            } else {
                logout_button?.visibility = View.GONE
            }
        }
    }

    private fun checkUserStoreState() {
        seller_account_button?.isEnabled = false
        store_button_progress_bar?.visibility = View.VISIBLE

        mAuth.currentUser?.uid.let {
            if (!it.isNullOrBlank()) {
                val docRef = db.collection("stores").document(it).get()
                docRef.addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) store =
                        snapshot.toObject(Store::class.java)
                    seller_account_button?.isEnabled = true
                    store_button_progress_bar?.visibility = View.GONE
                }.addOnFailureListener {
                    seller_account_button?.isEnabled = true
                    store_button_progress_bar?.visibility = View.GONE
                }
            } else {
                seller_account_button?.isEnabled = true
                store_button_progress_bar?.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        checkUserStoreState()
        super.onResume()
    }
}