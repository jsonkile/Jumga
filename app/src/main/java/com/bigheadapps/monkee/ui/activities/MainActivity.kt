package com.bigheadapps.monkee.ui.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bigheadapps.monkee.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var cartItemCountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottom_navigation?.setupWithNavController(navController)
        bottom_navigation?.setOnNavigationItemReselectedListener { }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
//        val cartItem = menu.findItem(menu[0].itemId)
//        val cartActionView = cartItem.actionView
//        cartItemCountTextView = cartActionView.findViewById(R.id.cart_badge)
//        updateCartBadge(0)
//
//        cartActionView.setOnClickListener {
//            onOptionsItemSelected(cartItem)
//        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.cart_action -> {
//                if (uid == null) {
//                    startActivity(Intent(this, AuthActivity::class.java))
//                } else {
//                    startActivity(Intent(this, OrdersActivity::class.java))
//                }
//            }

//            R.id.search_action -> {
//
//            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun updateCartBadge(value: Int) {
//        cartItemCountTextView.text = value.toString()
//    }
}