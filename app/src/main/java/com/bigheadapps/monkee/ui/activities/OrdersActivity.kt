package com.bigheadapps.monkee.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.problemToast
import com.bigheadapps.monkee.models.Order
import com.bigheadapps.monkee.ui.adapters.OrdersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_orders.*
import kotlinx.android.synthetic.main.activity_view_store.*

class OrdersActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private val ordersAdapter by lazy { OrdersAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        setSupportActionBar(findViewById(R.id.orders_tool_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        setupUI()
        getOrders()
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

    private fun setupUI() {
        recyclerView = my_orders_recycler_view
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ordersAdapter
        ordersAdapter.setOnItemClickListener(object : OrdersAdapter.ClickListener {
            override fun onClick(view: View, pos: Int, order: Order) {
                val intent =
                    Intent(this@OrdersActivity, ProductActivity::class.java)
                val bundle = Bundle()
                bundle.putString("image", order.image)
                bundle.putString("description", "")
                bundle.putString("name", order.productName)
                bundle.putInt("price", (order.totalCost - order.delivery).toInt())
                bundle.putInt("quantity", 0)
                bundle.putString("sellerId", order.sellerId)
                bundle.putString("id", order.product)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        })

        orders_page_refresh?.setOnRefreshListener {
            getOrders()
        }
    }

    private fun getOrders() {
        orders_page_refresh?.isRefreshing = true
        mAuth.currentUser?.uid.let { uid ->
            db.collection("orders").whereEqualTo("customerId", uid).get().addOnSuccessListener {
                orders_page_refresh?.isRefreshing = false
                if (it != null && !it.isEmpty) {
                    val orders = mutableListOf<Order>()
                    for (doc in it) {
                        orders.add(doc.toObject(Order::class.java))
                    }
                    ordersAdapter.inflate(orders)
                } else {
                    empty_orders_list?.visibility = View.VISIBLE
                }
            }.addOnFailureListener {
                orders_page_refresh?.isRefreshing = false
                problemToast()
            }
        }
    }
}