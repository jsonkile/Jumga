package com.bigheadapps.monkee.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.vvalidator.form
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.longToast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.add_to_inventory_form.*
import timber.log.Timber.d
import java.io.File
import java.util.*

class NewItemActivity : AppCompatActivity() {

    private var imageId = UUID.randomUUID().toString()
    private var selectedImagePath: String? = null
    private val storage = Firebase.storage
    var storageRef = storage.reference

    private val db = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()

    private val itemDocument = db.collection("products").document()
    private lateinit var cloudStoreImageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)

        setSupportActionBar(findViewById(R.id.add_item_tool_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }

        setupUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_to_inventory_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.add_image -> {
                startImagePicker()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startImagePicker() {
        ImagePicker.with(this)
            .crop(
                4f,
                5f
            )
            .compress(100)
            .galleryMimeTypes(
                mimeTypes = arrayOf(
                    "image/jpg",
                    "image/jpeg"
                )
            )
            .start()
    }

    private fun setupUI() {
        val items = listOf("Food & Drinks", "Fashion", "Gadgets", "Others")
        val adapter = ArrayAdapter(this, R.layout.category_list_item, items)
        (item_category_input_layout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        form {
            input(R.id.item_name_input) {
                isNotEmpty()
            }

            input(R.id.item_category_input) {
                isNotEmpty()
            }

            input(R.id.item_description_input) {
                isNotEmpty()
            }

            input(R.id.item_price_input) {
                isNumber().atLeast(1)
                isNotEmpty()
            }

            input(R.id.item_stock_quantity_input) {
                isNotEmpty()
                isNumber()
            }

            submitWith(R.id.upload_item_button) {
                loading()
                if (it.success()) {
                    startProcess()
                }
            }
        }
    }

    private fun startProcess() {
        if (!selectedImagePath.isNullOrBlank()) {
            startImageUpload(selectedImagePath!!)
        } else if (selectedImagePath.isNullOrBlank()) {
            endLoading()
            MaterialDialog(this).show {
                title(R.string.oops)
                message(R.string.please_select_an_image)
                positiveButton(R.string.okay)
            }
        }
    }

    private fun loading() {
        item_name_input?.isEnabled = false
        item_category_input?.isEnabled = false
        item_description_input?.isEnabled = false
        item_price_input?.isEnabled = false
        item_stock_quantity_input?.isEnabled = false


        upload_item_button?.text = resources.getString(R.string.uploading)
        upload_item_button?.isEnabled = false
    }

    private fun endLoading() {
        item_name_input?.isEnabled = true
        item_category_input?.isEnabled = true
        item_description_input?.isEnabled = true
        item_price_input?.isEnabled = true
        item_stock_quantity_input?.isEnabled = true

        upload_item_button?.text = resources.getString(R.string.upload)
        upload_item_button?.isEnabled = true
    }

    private fun addItemToFirestore(cloudStoreImagePath: String) {

        val category = when (item_category_input?.text.toString()) {
            "Food & Drinks" -> 0
            "Fashion" -> 1
            "Gadgets" -> 2
            "Others" -> 3
            else -> 3
        }

        mAuth.currentUser?.uid?.let {

            val item = hashMapOf(
                "name" to item_name_input?.text.toString(),
                "quantity" to item_stock_quantity_input?.text.toString().toInt(),
                "description" to item_description_input?.text.toString(),
                "category" to category,
                "price" to item_price_input?.text.toString().toInt(),
                "sellerId" to it,
                "picture" to cloudStoreImagePath,
                "status" to 0
            )


            itemDocument.set(item)
                .addOnSuccessListener {
                    //Done, close activity
                    finish()
                }
                .addOnFailureListener { e ->
                    endLoading()

                    //Delete image from storage

                    MaterialDialog(this).show {
                        title(R.string.oops)
                        message(R.string.something_went_wrong)
                        positiveButton(R.string.okay) {
                            startProcess()
                        }
                    }
                    d("Failed to add item: ${e.message.toString()}")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Update image view with Image Uri
                val fileUri = data?.data
                item_image_selector?.setImageURI(fileUri)
                if (item_image_selector_card?.visibility != View.VISIBLE) item_image_selector_card?.visibility =
                    View.VISIBLE

                //File Path
                selectedImagePath = ImagePicker.getFilePath(data)!!
            }
            ImagePicker.RESULT_ERROR -> {
                longToast("Error occurred when picking the image. Please try again.")
            }
            else -> {
                d("Image picker task was cancelled.")
            }
        }
    }

    private fun startImageUpload(localPath: String) {
        val file = Uri.fromFile(File(localPath))
        cloudStoreImageReference = storageRef.child("product-images/$imageId")
        val uploadTask = cloudStoreImageReference.putFile(file)

        uploadTask.addOnFailureListener {
            longToast("The image upload failed. Please try again")
            endLoading()
        }.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                addItemToFirestore(it.toString())
            }
                .addOnFailureListener {
                    cloudStoreImageReference.delete()
                    longToast("The image upload failed. Please try again")
                }
        }
    }
}