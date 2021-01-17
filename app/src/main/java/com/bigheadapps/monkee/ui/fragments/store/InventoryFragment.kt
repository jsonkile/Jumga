package com.bigheadapps.monkee.ui.fragments.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.longToast
import com.bigheadapps.monkee.helpers.problemToast
import com.bigheadapps.monkee.models.Product
import com.bigheadapps.monkee.models.Store
import com.bigheadapps.monkee.ui.activities.NewItemActivity
import com.bigheadapps.monkee.ui.adapters.ProductsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.empty_inventory.*
import kotlinx.android.synthetic.main.fragment_inventory.*
import timber.log.Timber

class InventoryFragment : Fragment() {

    private val db = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var inventoryListenerRegistration: ListenerRegistration
    private lateinit var storeListenerRegistration: ListenerRegistration
    private lateinit var recyclerView: RecyclerView
    private val productsAdapter by lazy { ProductsAdapter() }

    private var store = Store()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    /**
     * Get inventory items and monitor for changes
     */
    private fun monitorInventory() {
        val inventoryRef =
            db.collection("products").whereEqualTo("sellerId", mAuth.currentUser!!.uid)
        inventoryListenerRegistration = inventoryRef.addSnapshotListener { value, error ->
            if (error != null) {
                this.requireContext().longToast("Failed to fetch your inventory.")
                Timber.d("Error finding inventory: ${error.message.toString()}")
            } else {
                val products = mutableListOf<Product>()
                for (doc in value!!) {
                    products.add(doc.toObject(Product::class.java))
                }
                updateUIWithProducts(products)
            }
        }
    }

    private fun setupUI() {
        add_to_inventory_fab?.setOnClickListener {
            if (store.hasPaidSetupFee) {
                startActivity(Intent(this.requireActivity(), NewItemActivity::class.java))
            } else {
                requireContext().longToast("Please complete your store setup under 'Account'")
            }
        }

        add_to_inventory_button?.setOnClickListener {
            if (store.hasPaidSetupFee) {
                startActivity(Intent(this.requireActivity(), NewItemActivity::class.java))
            } else {
                requireContext().longToast("Please complete your store setup under 'Account'")
            }
        }

        recyclerView = inventory_recycler_view
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = productsAdapter

        productsAdapter.setOnItemClickListener(object : ProductsAdapter.ClickListener {
            override fun onClick(view: View, pos: Int, product: Product) {

            }
        })
    }

    private fun updateUIWithProducts(products: MutableList<Product>) {
        if (products.isEmpty()) {
            empty_inventory_layout?.visibility = View.VISIBLE
            inventory_recycler_view?.visibility = View.GONE
        } else {
            empty_inventory_layout?.visibility = View.GONE
            inventory_recycler_view?.visibility = View.VISIBLE
            productsAdapter.inflate(products)
        }
    }

    /**
     * Get the store information and keep monitoring it for changes from the server
     */
    private fun monitorStoreData() {
        mAuth.currentUser?.uid?.let {
            val docRef = db.collection("stores").document(it)
            storeListenerRegistration = docRef.addSnapshotListener { value, error ->
                if (error != null) {
                    //Handle error
                    activity?.problemToast()
                } else if (value != null && value.exists()) {
                    store = value.toObject(Store::class.java) ?: Store()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        monitorInventory()
        monitorStoreData()
    }

    override fun onStop() {
        super.onStop()
        inventoryListenerRegistration.remove()
        storeListenerRegistration.remove()
    }
}