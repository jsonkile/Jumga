package com.bigheadapps.monkee.ui.fragments.main

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
import com.bigheadapps.monkee.models.Product
import com.bigheadapps.monkee.ui.activities.ProductActivity
import com.bigheadapps.monkee.ui.adapters.ProductsAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_gadgets.*
import kotlinx.android.synthetic.main.fragment_inventory.*
import kotlinx.android.synthetic.main.fragment_others.*
import timber.log.Timber

class OthersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val productsAdapter by lazy { ProductsAdapter() }

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_others, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        getProducts()
    }

    private fun setupUI() {
        requireActivity().findViewById<MaterialToolbar>(R.id.main_toolbar)
            .setTitle(R.string.others_fragment_heading)

        recyclerView = others_recycler_view
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = productsAdapter
        productsAdapter.setOnItemClickListener(object : ProductsAdapter.ClickListener {
            override fun onClick(view: View, pos: Int, product: Product) {
                val intent =
                    Intent(this@OthersFragment.requireActivity(), ProductActivity::class.java)
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

        others_page_refresh?.setOnRefreshListener {
            getProducts()
        }
    }

    private fun getProducts() {
        others_page_refresh?.isRefreshing = true
        db.collection("products")
            .whereEqualTo("category", 3)
            .get()
            .addOnCompleteListener {
                others_page_refresh?.isRefreshing = false
            }
            .addOnSuccessListener {
                val products = mutableListOf<Product>()
                for (doc in it.documents) {
                    val product = doc.toObject(Product::class.java)
                    product?.id = doc.id
                    products.add(product ?: Product())
                }
                updateUI(products)
            }.addOnFailureListener {
                requireActivity().longToast("Failed to load data. Check internet connection.")
                Timber.d("Error finding products: ${it.message.toString()}")
            }
    }

    private fun updateUI(products: List<Product>) {
        productsAdapter.inflate(products)
    }
}