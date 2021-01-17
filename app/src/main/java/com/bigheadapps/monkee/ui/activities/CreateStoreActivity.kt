package com.bigheadapps.monkee.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.vvalidator.form
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.models.Rider
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.store_creation_form.*
import timber.log.Timber
import timber.log.Timber.d

class CreateStoreActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()
    private var dispatchRiders = mutableListOf<Rider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_store)
        setSupportActionBar(findViewById(R.id.create_store_tool_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        setupUI()
        getDispatchRiders()
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
        form {
            input(R.id.store_name_input) {
                isNotEmpty()
                length().atLeast(3)
            }

            input(R.id.store_phone_input) {
                isNotEmpty()
                isNumber()
                length().exactly(11)
            }

            input(R.id.store_address_input) {
                isNotEmpty()
                length().atLeast(15)
            }

            input(R.id.store_description_input) {
                isNotEmpty()
                length().atLeast(10)
            }

            submitWith(R.id.create_store_button) {
                createNewStore(
                    store_name_input?.text.toString(),
                    store_phone_input?.text.toString(),
                    store_address_input?.text.toString(),
                    store_description_input?.text.toString()
                )
            }
        }
    }

    //Get sub account id of dispatch riders
    private fun getDispatchRiders() {
        db.collection("helpers").document("app").collection("dispatch-riders").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    dispatchRiders.clear()
                    for (rider in it) {
                        dispatchRiders.add(rider.toObject(Rider::class.java))
                    }
                }
            }.addOnFailureListener {
                d(it.message.toString())
            }
    }

    private fun createNewStore(name: String, phone: String, address: String, description: String) {
        val store = hashMapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "description" to description,
            "createdAt" to FieldValue.serverTimestamp()
        )

        if (!dispatchRiders.isNullOrEmpty()) {

            mAuth.currentUser?.uid?.let { uid ->
                addNewStoreToFirebase(dispatchRiders, uid, store)
                    .addOnSuccessListener {

                        Timber.d("New store was created!")
                        startActivity(Intent(this, StoreActivity::class.java))
                        finish()

                    }.addOnFailureListener { e ->

                        //Retry
                        MaterialDialog(this).show {
                            title(R.string.oops)
                            message(R.string.something_went_wrong)
                            positiveButton(R.string.okay) {
                                addNewStoreToFirebase(dispatchRiders, uid, store)
                            }
                        }

                        //Log error
                        Timber.d("Failed to create new score: ${e.message.toString()}")
                    }
            }
        } else {
            MaterialDialog(this).show {
                title(R.string.oops)
                message(R.string.something_went_wrong)
                positiveButton(R.string.okay) {
                    addNewStoreToFirebase(dispatchRiders, mAuth.currentUser!!.uid, store)
                }
            }
            getDispatchRiders()
        }
    }

    private fun addNewStoreToFirebase(
        riders: List<Rider>,
        uid: String,
        store: HashMap<String, Any>
    ): Task<Void> {
        val storesRef = db.collection("stores").document(uid)
        val helperRef = db.collection("helpers").document("app")
        return db.runTransaction {

            //Last test account created
            val lastTestAccount = it.get(helperRef)["lastTestAccount"].toString()

            //Make new number
            val newAccount = "0${lastTestAccount.toInt() + 1}"

            val data = hashMapOf(
                "lastTestAccount" to newAccount.toString()
            )

            //Save latest account in helper
            it.set(helperRef, data)

            //Save latest account number in new store, save AccountId of random rider
            val rider = riders.random()

            store["accountNumber"] = newAccount.toString()
            store["riderEmail"] = rider.email
            store["riderName"] = rider.name
            store["riderSubAccountId"] = rider.subAccountId

            //save store
            it.set(storesRef, store)

            null
        }
    }
}