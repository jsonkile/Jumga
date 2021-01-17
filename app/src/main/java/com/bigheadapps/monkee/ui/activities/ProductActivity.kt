package com.bigheadapps.monkee.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.afollestad.materialdialogs.MaterialDialog
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.DELIVERY_FEE
import com.bigheadapps.monkee.helpers.Functions
import com.bigheadapps.monkee.helpers.Raver
import com.bigheadapps.monkee.helpers.problemToast
import com.bigheadapps.monkee.models.Billing
import com.bigheadapps.monkee.models.Product
import com.bigheadapps.monkee.models.Store
import com.bumptech.glide.Glide
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.rave_java_commons.Meta
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants
import com.flutterwave.raveandroid.rave_java_commons.SubAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.product_information.*
import timber.log.Timber.d
import java.util.*


class ProductActivity : AppCompatActivity() {

    //Holds the current Firebase firestore (DB) instance
    private val db = Firebase.firestore

    //Hold the authentication state instance
    private val mAuth = FirebaseAuth.getInstance()

    //Create a product variable for the item in view and instantiate it to null
    private var product: Product? = null

    //Create a store variable for the seller (store) and instantiate to null
    private var store: Store? = null

    //DB id of the product
    private lateinit var id: String

    //Link to the picture of the product
    private lateinit var picture: String

    //The user id of the seller (store)
    private lateinit var storeId: String

    //When the page is started, the number of items in cart should be 1
    private var quantityInCart = 1

    //A live data to hold the value of the items in cart + delivery fee ($50)
    private val totalAmountLiveData = MutableLiveData(1.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        setSupportActionBar(findViewById(R.id.product_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        getBundle()
    }


    /**
     * Receive bits of information of the product to show from the previous activity
     * product id, image link and the seller or store id
     */

    private fun getBundle() {
        val bundles = intent.extras
        id = bundles?.getString("id") ?: ""
        picture = bundles?.getString("image") ?: ""
        storeId = bundles?.getString("sellerId") ?: ""

        //if the info is actually received correctly the use them to continue
        if (id.isNotBlank() && picture.isNotBlank() && storeId.isNotBlank()) {
            //show the product image with the link provided
            Glide.with(this).load(picture).placeholder(R.drawable.placeholder_image).centerCrop()
                .into(product_image)

            //Hide parts of the product page until the information is gotten from the server
            product_page?.visibility = View.GONE

            //Load the product information
            loadStuff()
        } else {
            //If the activity did not get the bits, the show error and exit
            problemToast()
            finish()
        }
    }

    /**
     * Load the product information from firebase and indicate 'loading' and 'done' to the user
     */
    private fun loadStuff() {
        product_refreshLayout?.isRefreshing = true
        getStoreAndSellerInformationFromFirebase().addOnFailureListener {
            product_refreshLayout?.isRefreshing = false
            problemToast()
        }.addOnSuccessListener {
            if (store != null && product != null) {
                product_refreshLayout?.isRefreshing = false
                product_page?.visibility = View.VISIBLE

                //Once the information of the product is gotten from firebase, setup the page and update the UI with the information
                setupUI()
                setupObservers()
                updateUiWithProductAndSellerInformation()
            }
        }
    }

    /**
     *The main logic for query firebase for the product and seller information
     */
    private fun getStoreAndSellerInformationFromFirebase(
    ): Task<Void> {
        val storeRef = db.collection("stores").document(storeId)
        val productRef = db.collection("products").document(id)
        return db.runTransaction {

            store = it.get(storeRef).toObject(Store::class.java)
            product = it.get(productRef).toObject(Product::class.java)

            null
        }
    }


    /**
     * Set the UI components to work
     */
    private fun setupUI() {
        product_page?.visibility = View.VISIBLE

        product_refreshLayout?.setOnRefreshListener {
            loadStuff()
        }

        //Adds one unit of the item to the cart
        plus_button?.setOnClickListener {
            quantityInCart++
            counter_text?.text =
                resources.getString(R.string.cost, (product!!.price * quantityInCart))
            totalAmountLiveData.postValue((product!!.price * quantityInCart) + DELIVERY_FEE)
        }

        //removes one unit of the item from cart
        minus_button?.setOnClickListener {
            if (quantityInCart > 1) {
                quantityInCart--
                counter_text?.text =
                    resources.getString(R.string.cost, (product!!.price * quantityInCart))
                totalAmountLiveData.postValue((product!!.price * quantityInCart) + DELIVERY_FEE)
            }
        }

        //proceed to payment when this button is click
        product_make_order_button?.setOnClickListener {
            if (mAuth.currentUser?.uid == null) {
                startActivity(Intent(this, AuthActivity::class.java))
            } else {
                checkBillingInformation()
            }
        }

        //Load the seller page when the seller tag is clicked
        seller?.setOnClickListener {
            if (storeId.isNotBlank()) {
                val intent =
                    Intent(this, ViewStoreActivity::class.java)
                val bundle = Bundle()
                bundle.putString("id", storeId)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        }

        seller?.text = resources.getString(R.string.default_text_underlined, "Merchant")
        total_charge_label?.text =
            resources.getString(R.string.total_charge, quantityInCart, DELIVERY_FEE.toInt())
        counter_text?.text = resources.getString(R.string.cost, (product!!.price * quantityInCart))
    }


    /**
     *Once we have the information from the server, update the UI with it
     */
    private fun updateUiWithProductAndSellerInformation() {
        totalAmountLiveData.postValue(product!!.price + DELIVERY_FEE)

        Glide.with(this).load(product!!.picture).centerCrop().into(product_image)

        name?.text = product!!.name
        stock?.text = when {
            product!!.quantity > 10 -> resources.getString(R.string.available_on_demand)
            else -> resources.getString(R.string.product_stock, product!!.quantity)
        }
        price?.text = resources.getString(R.string.default_text, Functions.dollafy(product!!.price))
        completed_orders?.text = resources.getString(R.string.product_completed_orders, 0)
        description?.text = resources.getString(R.string.default_text, product!!.description)

        counter_text?.text = resources.getString(R.string.cost, (product!!.price * quantityInCart))

        seller?.text = resources.getString(R.string.default_text, store!!.name)
    }


    /**
     * Have the total amount shown in the view to be updated once the amount changes
     */
    private fun setupObservers() {
        totalAmountLiveData.observeForever {
            product_make_order_button?.text =
                resources.getString(R.string.proceed_with_amount, it.toInt())
            total_charge_label?.text =
                resources.getString(R.string.total_charge, quantityInCart, DELIVERY_FEE.toInt())
        }
    }

    /**
     * Check if billing information of the user is complete, if it is, send them to pay else send them to settings account
     */
    private fun checkBillingInformation() {
        val billing = Functions.isBillingOkay(this)
        if (billing != null) {
            if (store != null) {
                startRave(billing)
            } else {
                MaterialDialog(this).show {
                    title(R.string.oops)
                    message(R.string.something_went_wrong)
                    positiveButton(R.string.okay) {
                        checkBillingInformation()
                    }
                }
            }
        } else {
            MaterialDialog(this).show {
                title(R.string.missing_details)
                message(R.string.billing_error_message)
                positiveButton(R.string.go_to_settings) {
                    startActivity(Intent(this@ProductActivity, SettingsActivity::class.java))
                }
            }
        }
    }

    /**
     * Actually start Flutterwave rave for receiving payments
     * @param billing Holds the billing information of the user
     */
    private fun startRave(billing: Billing) {
        //Meta data with extra important info to send to flutterwave
        val metadata =
            listOf(
                Meta("uid", mAuth.currentUser!!.uid),
                Meta("productId", product!!.id),
                Meta("sellerId", product!!.sellerId),
                Meta("merchantSubAccount", store!!.subAccountId),
                Meta("sellerName", store!!.name),
                Meta("productName", product!!.name),
                Meta("image", product!!.picture),
            )

        //Calculate the merchants share and dispatcher share from the total amount and create the sub account variables
        val totalAmount = totalAmountLiveData.value!!.toDouble()
        val merchantAccount =
            SubAccount(
                store?.subAccountId,
                "1",
                "flat_subaccount",
                Functions.convertAmount(Functions.getMerchantCut(totalAmount), billing).toString()
            )

        val dispatcherAccount =
            SubAccount(
                store?.riderSubAccountId,
                "1",
                "flat_subaccount",
                Functions.convertAmount(Functions.getDispatcherCut(), billing).toString()
            )

        val subAccounts = listOf(merchantAccount, dispatcherAccount)


        //start rave
        Raver.deliverRaveInstance(
            activity = this@ProductActivity,
            amount = totalAmount,
            billing = billing,
            units = 1,
            productName = "Store setup fee",
            metadata = metadata,
            subAccounts = subAccounts
        ).initialize()
    }


    //Record the payment information when the payment is complete
    private fun writeCompletedOrderToFirebase() {
        val billing = Functions.isBillingOkay(this)

        val order = hashMapOf(
            "product" to id,
            "productName" to product!!.name,
            "image" to product!!.picture,
            "customerId" to mAuth.currentUser!!.uid,
            "customerName" to billing?.fname + billing?.lname,
            "customerEmail" to billing?.email,
            "customerPhoneNumber" to billing?.phone,
            "customerBillingAddress" to billing?.address,
            "customerCountry" to billing?.country,
            "sellerId" to product!!.sellerId,
            "sellerName" to store!!.name,
            "quantity" to quantityInCart,
            "delivery" to DELIVERY_FEE,
            "hasPaidForOrder" to true,
            "merchantCut" to Functions.getMerchantCut(totalAmountLiveData.value!!.toDouble()),
            "dispatcherCut" to Functions.getDispatcherCut(),
            "totalCost" to totalAmountLiveData.value,
            "paidAt" to FieldValue.serverTimestamp(),
        )

        db.collection("orders").document().set(order).addOnFailureListener {
            d(it.message.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.product_page_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    //Receives the status of the payment from the Rave page
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            val message = data.getStringExtra("response")
            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    writeCompletedOrderToFirebase()
                    MaterialDialog(this).show {
                        title(R.string.completed_order)
                        message(R.string.completed_order_message)
                        positiveButton(R.string.done) {
                            this.dismiss()
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

                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }


            R.id.seller_information -> {
                if (storeId.isNotBlank()) {
                    val intent =
                        Intent(this, ViewStoreActivity::class.java)
                    val bundle = Bundle()
                    bundle.putString("id", storeId)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}