package com.bigheadapps.monkee.helpers

import android.content.Context
import android.widget.Toast

fun Context.longToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG)
        .show()
}

fun Context.shortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
        .show()
}

fun Context.problemToast() {
    Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT)
        .show()
}