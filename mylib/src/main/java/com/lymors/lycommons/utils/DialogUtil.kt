package com.lymors.lycommons.utils
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtil {

    fun Context.alertDialog(
        title: String = "Confirm",
        message: String = "Are you sure you want to proceed?",
        positiveText: String = "Ok",
        negativeText: String = "Cancel"
    ): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null)
            .create()
    }


    fun Context.alertDialogMaterial(
        title: String,
        message: String,
        positiveText: String = "Ok",
        negativeText: String = "Cancel"
    ): androidx.appcompat.app.AlertDialog {
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null)
        return alertDialogBuilder.create()
    }


    fun android.app.AlertDialog.setOnPositiveListener(onConfirm: () -> Unit) {
        setButton(DialogInterface.BUTTON_POSITIVE, "Ok") { dialog, _ ->
            onConfirm()
        }
    }

    fun android.app.AlertDialog.setOnNegativeListener(onCancel: () -> Unit ) {
        setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
            onCancel()
        }
    }

    fun androidx.appcompat.app.AlertDialog.setOnPositiveListener(onConfirm: () -> Unit) {
        setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { dialog, _ ->
            onConfirm()
        }
    }

    fun androidx.appcompat.app.AlertDialog.setOnNegativeListener(onCancel: () -> Unit) {
        setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
            onCancel()
        }
    }


    fun Context.progressDialog(
        message: String = "Please wait...",
        transparent: Boolean = false,
    ): ProgressDialog {
        val progressDialog = ProgressDialog(this)
        if (transparent) {
            progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setCancelable(false)
        progressDialog.setMessage(message)
        return progressDialog
    }




    fun Context.progressDialogMaterial(message: String = "Please wait.."): androidx.appcompat.app.AlertDialog {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }
        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }
        val textView = TextView(this).apply {
            text = message
            setPadding(20, 20, 20, 20)
            gravity = Gravity.CENTER
        }
        layout.addView(progressBar)
        layout.addView(textView)

        val progressDialog = MaterialAlertDialogBuilder(this)
            .setView(layout)
            .setCancelable(false)
            .create()
        return progressDialog
    }


    inline fun <T : ViewBinding> Context.showCustomLayoutDialog(
        crossinline bindingInflater: (LayoutInflater) -> T,
        gravity: Int = Gravity.CENTER,
        isCancelable:Boolean = true,
        crossinline callback: (T, PopupWindow) -> Unit = { _, _ -> }
    ):PopupWindow {
        val inflater: LayoutInflater = LayoutInflater.from(this)
        val binding: T = bindingInflater(inflater)
        val screenWidth = (this.resources.displayMetrics).widthPixels
        val width = (screenWidth * 0.9).toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(binding.root, width, height, true)
        popupWindow.isOutsideTouchable = isCancelable
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this,
                android.R.color.transparent
            )
        )
        popupWindow.showAtLocation(binding.root, gravity, 0, 0)
        callback(binding, popupWindow)
        return popupWindow

    }



    inline fun <T : ViewBinding> View.showCustomPopup(
        crossinline bindingInflater: (LayoutInflater) -> T,
        crossinline callback: (T, PopupWindow) -> Unit = { _, _ -> }
    ) {
        setOnClickListener {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val binding: T = bindingInflater(inflater)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(binding.root, width, height, true)
            popupWindow.elevation = 20f
            popupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    android.R.color.transparent
                )
            )
            popupWindow.showAsDropDown(this)
            callback(binding, popupWindow)
        }
    }



    fun <T : ViewBinding> Context.showBottomSheet(
        bindingInflater: (LayoutInflater) -> T,
        callback: (T, BottomSheetDialog) -> Unit = { _, _ -> }
    ): BottomSheetDialog {
        val bottomSheetDialog = BottomSheetDialog(this)
        val binding = bindingInflater(LayoutInflater.from(this))
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bottomSheetDialog.setOnShowListener {
            val roundedBackground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(80f, 80f, 80f, 80f, 0f, 0f, 0f, 0f)
            }
            binding.root.setBackgroundDrawable(roundedBackground)
        }

        bottomSheetDialog.show()
        callback(binding, bottomSheetDialog)
        return bottomSheetDialog
    }






}