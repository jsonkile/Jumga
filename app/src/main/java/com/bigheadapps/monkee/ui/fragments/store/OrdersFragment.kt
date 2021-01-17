package com.bigheadapps.monkee.ui.fragments.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.problemToast
import com.bigheadapps.monkee.models.Order
import com.bigheadapps.monkee.models.Store
import com.bigheadapps.monkee.ui.adapters.MerchantOrdersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_orders.*


class OrdersFragment : Fragment() {

    private val db = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()
    private var store = Store()

    private lateinit var recyclerView: RecyclerView
    private val merchantOrdersAdapter by lazy { MerchantOrdersAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        getStoreData()
        getOrders()
    }

    private fun setupUI() {
        recyclerView = orders_recycler_view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = merchantOrdersAdapter
    }


    /**
     * Get store information from firebase
     */
    private fun getStoreData() {
        mAuth.currentUser?.uid?.let {
            val docRef = db.collection("stores").document(it).get()
            docRef.addOnSuccessListener { data ->
                if (data != null && data.exists()) {
                    store = data.toObject(Store::class.java) ?: Store()
                    updateUI()
                } else {
                    requireContext().problemToast()
                }
            }.addOnFailureListener {
                requireContext().problemToast()
            }
        }
    }

    /**
     * Get all orders made on store items
     */
    private fun getOrders() {
        mAuth.currentUser?.uid?.let {
            val docRef = db.collection("orders").whereEqualTo("sellerId", it).get()
            docRef.addOnSuccessListener { data ->
                val orders = mutableListOf<Order>()
                for (doc in data) {
                    orders.add(doc.toObject(Order::class.java))
                }
                merchantOrdersAdapter.inflate(orders)
            }.addOnFailureListener {
                requireContext().problemToast()
            }
        }
    }

    private fun updateUI() {
        dispatcher_name?.text = resources.getString(R.string.default_text, store.riderName)
    }
}