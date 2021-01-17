package com.bigheadapps.monkee.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.shortToast
import com.bigheadapps.monkee.models.Order
import com.bigheadapps.monkee.models.Product
import com.bigheadapps.monkee.models.Store
import com.bigheadapps.monkee.ui.viewmodels.APIViewModel
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants
import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_store.*
import org.koin.android.viewmodel.ext.android.viewModel

class StoreActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var storeListenerRegistration: ListenerRegistration
    private lateinit var inventoryListenerRegistration: ListenerRegistration
    private lateinit var ordersListenerRegistration: ListenerRegistration
    private lateinit var ordersBadgeDrawable: BadgeDrawable
    private lateinit var inventoryBadgeDrawable: BadgeDrawable
    private var store = Store()

    private val apiViewModel: APIViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        setSupportActionBar(findViewById(R.id.store_tool_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        setupNavigation()
        setupUi()
        monitorStoreDetails()
        monitorInventory()
        monitorOrders()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.store_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        store_bottom_navigation?.setupWithNavController(navController)
        store_bottom_navigation?.setOnNavigationItemReselectedListener { }
    }

    private fun setupUi() {
        ordersBadgeDrawable = store_bottom_navigation
            .getOrCreateBadge(R.id.orders_fragment)
        ordersBadgeDrawable.isVisible = true
        ordersBadgeDrawable.backgroundColor = resources.getColor(R.color.red)
        ordersBadgeDrawable.badgeTextColor = resources.getColor(R.color.white)
        ordersBadgeDrawable.number = 0

        inventoryBadgeDrawable = store_bottom_navigation
            .getOrCreateBadge(R.id.inventory_fragment)
        inventoryBadgeDrawable.isVisible = true
        inventoryBadgeDrawable.backgroundColor = resources.getColor(R.color.red)
        inventoryBadgeDrawable.badgeTextColor = resources.getColor(R.color.white)
        inventoryBadgeDrawable.number = 0
    }

    private fun monitorInventory() {
        inventoryListenerRegistration =
            db.collection("products").whereEqualTo("sellerId", mAuth.currentUser!!.uid)
                .addSnapshotListener { value, error ->
                    if (error == null && value != null) {
                        val products = mutableListOf<Product>()
                        for (doc in value) {
                            products.add(doc.toObject(Product::class.java))
                        }
                        inventoryBadgeDrawable.number = products.size
                    }
                }
    }

    private fun monitorOrders() {
        ordersListenerRegistration =
            db.collection("orders").whereEqualTo("sellerId", mAuth.currentUser!!.uid)
                .addSnapshotListener { value, error ->
                    if (error == null && value != null) {
                        val orders = mutableListOf<Order>()
                        for (doc in value) {
                            orders.add(doc.toObject(Order::class.java))
                        }
                        ordersBadgeDrawable.number = orders.size
                    }
                }
    }

    private fun monitorStoreDetails() {
        storeListenerRegistration = db.collection("stores").document(mAuth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error == null) {
                    if (value != null && value.exists()) {
                        store = value.toObject(Store::class.java) ?: Store()
                        updateUIWithStore(store)
                    }
                }
            }
    }

    private fun updateUIWithStore(store: Store) {
        supportActionBar?.subtitle = store.name
    }

    override fun onStop() {
        storeListenerRegistration.remove()
        ordersListenerRegistration.remove()
        inventoryListenerRegistration.remove()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            val message = data.getStringExtra("response")
            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    val storesRef = db.collection("stores").document(mAuth.currentUser!!.uid)
                    storesRef.update(
                        hashMapOf(
                            "hasPaidSetupFee" to true
                        ) as Map<String, Any>
                    )

                    MaterialDialog(this).show {
                        title(R.string.completed_order)
                        message(R.string.completed_setup_payment_message)
                        positiveButton(R.string.done) {
                            this.dismiss()
                            startActivity(intent)
                            finish()
                            overridePendingTransition(0, 0);
                        }
                    }
                }
                RavePayActivity.RESULT_ERROR -> {
                    MaterialDialog(this).show {
                        title(R.string.failed_order)
                        message(text = message)
                        positiveButton(R.string.okay) {
                            this.dismiss()
                        }
                    }
                }
                RavePayActivity.RESULT_CANCELLED -> {
                    shortToast("Cancelled")
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}