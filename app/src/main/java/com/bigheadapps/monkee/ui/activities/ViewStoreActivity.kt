package com.bigheadapps.monkee.ui.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.models.Product
import com.bigheadapps.monkee.models.Store
import com.bigheadapps.monkee.ui.adapters.ProductsAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_view_store.*
import kotlinx.android.synthetic.main.fragment_food.*

class ViewStoreActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var id: String

    private lateinit var recyclerView: RecyclerView
    private val productsAdapter by lazy { ProductsAdapter() }
    private var store = Store()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_store)
        setSupportActionBar(findViewById(R.id.view_store_tool_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        getBundle()
        setupUi()
        loadStuff()
    }

    private fun getBundle() {
        val bundles = intent.extras
        id = bundles?.getString("id") ?: ""
    }

    private fun loadStuff() {
        if (id.isNotBlank()) {
            getStore()
            getInventory()
        }
    }

    private fun getStore() {
        db.collection("stores").document(id).get()
            .addOnSuccessListener {
                if (it != null && it.exists()) {
                    store = it.toObject(Store::class.java) ?: Store()
                    updateUi(store)
                }
            }.addOnFailureListener { }
    }

    private fun getInventory() {
        db.collection("products").whereEqualTo("sellerId", id).get().addOnSuccessListener {
            if (it != null && !it.isEmpty) {
                val products = mutableListOf<Product>()
                for (doc in it) {
                    products.add(doc.toObject(Product::class.java))
                }
                productsAdapter.inflate(products)
            }
        }.addOnFailureListener {

        }
    }

    private fun updateUi(store: Store) {
        store_content?.visibility = View.VISIBLE
        store_name?.text = store.name
        store_description?.text = store.description
        store_physical_address?.text = store.address
    }

    private fun setupUi() {
        store_content?.visibility = View.GONE

        store_phone_number?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", store.phone, null))
            startActivity(intent)
        }

        store_email?.setOnClickListener {
            val uri = Uri.parse("smsto:${store.phone}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", "Hello")
            startActivity(intent)
        }

        recyclerView = store_recycler_view
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = productsAdapter
        productsAdapter.setOnItemClickListener(object : ProductsAdapter.ClickListener {
            override fun onClick(view: View, pos: Int, product: Product) {
                val intent =
                    Intent(this@ViewStoreActivity, ProductActivity::class.java)
                val bundle = Bundle()
                bundle.putString("image", product.picture)
                bundle.putString("description", product.description)
                bundle.putString("name", product.name)
                bundle.putInt("price", product.price)
                bundle.putInt("quantity", product.quantity)
                bundle.putString("sellerId", product.sellerId)
                bundle.putString("id", product.id)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        })
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
}