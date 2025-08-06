package com.mievento.ideal.utils

import android.view.View
import android.widget.Toast
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun String.toDate(format: String = "yyyy-MM-dd"): Date? {
    return try {
        SimpleDateFormat(format, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun Date.toString(format: String = "dd/MM/yyyy"): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(this)
}