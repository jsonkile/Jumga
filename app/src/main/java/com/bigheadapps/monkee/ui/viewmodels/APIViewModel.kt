package com.bigheadapps.monkee.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bigheadapps.monkee.helpers.Status
import com.bigheadapps.monkee.repo.RaveRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber.d
import timber.log.Timber.e

class APIViewModel(application: Application, private val raveRepo: RaveRepo) :
    AndroidViewModel(application) {

    val db = Firebase.firestore

    fun createSubAccount(accountNumber: String, businessName: String, businessPhone: String, businessEmail: String) {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                val result = raveRepo.createSubAccount(
                    accountNumber = accountNumber,
                    businessName = businessName,
                    businessMobile = businessPhone,
                    businessEmail = businessEmail
                )

                if (result.status == Status.SUCCESS) {
                    val data = hashMapOf(
                        "subAccountRef" to result.data!!.data.id,
                        "subAccountId" to result.data.data.subAccountId
                    )
                    val storeRef = db.collection("stores")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    storeRef.update(data as Map<String, Any>).addOnFailureListener {
                        deleteSubAccount(result.data.data.id)
                    }
                } else {
                    e(result.message.toString())
                }
            }
        }
    }

    fun deleteSubAccount(id: String) {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                val result = raveRepo.deleteSubAccount(id)

                if (result.status == Status.ERROR) {
                    e(result.message.toString())
                }
            }
        }
    }

}