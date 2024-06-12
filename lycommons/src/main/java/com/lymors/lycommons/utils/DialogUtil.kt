package com.lymors.lycommons.utils


import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.lymors.lycommons.R
import com.lymors.lycommons.utils.MyExtensions.createButton
import com.lymors.lycommons.utils.MyExtensions.createLinearLayout
import com.lymors.lycommons.utils.MyExtensions.createTextView

class DialogUtil {

    lateinit var dialog: Dialog

    interface DialogClickListener {
        fun onClickYes(d: DialogInterface)
        fun onClickNo(d: DialogInterface)
    }

    interface EditTextDialogClickListener {
        fun onClickYes(d: DialogInterface, texts: ArrayList<String>)
        fun onClickNo(d: DialogInterface)
    }


    fun showAlertDialogRounded(context: Context,title: String, message: String, cancelable: Boolean = false) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setCancelable(cancelable)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        dialog = alertDialog
    }


    inline fun <reified T : ViewBinding> showCustomLayoutDialog(context: Activity,
         crossinline bindingInflater: (LayoutInflater) -> T,
         cancelable: Boolean = true ,
         callback: (T, Dialog ) -> Unit
    ): Dialog {
        val binding = bindingInflater.invoke((context).layoutInflater)
        dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setCancelable(cancelable)
            .show()

        // Set the dialog background to white
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.rounderd_corner))

        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        callback.invoke( binding , dialog)
        return dialog
    }



    inline fun <reified T : ViewBinding> showCustomLayoutDialog(context: Activity,
        crossinline bindingInflater: (LayoutInflater) -> T,
        cancelable: Boolean = true
    ): T {
        val binding = bindingInflater.invoke((context).layoutInflater)
        dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setCancelable(cancelable)
            .show()

        // Set the dialog background to white
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.rounderd_corner))

        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        return binding
    }


    fun showAlertDialog(context: Context,title: String, message: String, cancelable: Boolean = false) {
        val alertDialogBuilder = createAlertDialog(context,title, message, cancelable)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    fun showInfoDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonLabel: String = "Ok",
        negativeButtonLabel: String = "Cancel",
        cancelable: Boolean,
        obj: DialogClickListener
    ) {
        val alertDialogBuilder = createAlertDialog(context , title, message, cancelable)
        alertDialogBuilder.setPositiveButton(positiveButtonLabel) { dialog, _ ->
            obj.onClickYes(dialog)
        }

        alertDialogBuilder.setNegativeButton(negativeButtonLabel) { dialog, _ ->
            obj.onClickNo(dialog)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    fun showEditTextDialog(
        context: Context,
        title: String,
        hints: List<String>,
        obj: EditTextDialogClickListener
    ) {
        val textFields = ArrayList<String>()
        val dialog = createAlertDialog(context,"", "")
        val verticalLinearLayout =
            createLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER)

        // Set title
        val titleTextView = createTextView(
            context,
            title,
            Gravity.CENTER_HORIZONTAL,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            30f,
            0,
            25,
            0,
            10,
            true
        )
        titleTextView.setTextColor(ContextCompat.getColor(context, R.color.cement))
        verticalLinearLayout.addView(titleTextView)

        // Set all TextInputLayouts
        val editTextLinearLayout =
            createLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER)
        hints.forEach { hint ->
            val textInputLayout = TextInputLayout(context)

            textInputLayout.apply {

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(20, 10, 20, 0)
                }
                // Set hint text color using ColorStateList
                val hintColor = ContextCompat.getColor(context, R.color.cement)
                val hintColorStateList = ColorStateList.valueOf(hintColor)
                textInputLayout.defaultHintTextColor = hintColorStateList
                boxStrokeWidth = 2
                boxStrokeColor = ContextCompat.getColor(context, R.color.cement)
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                boxBackgroundColor = ContextCompat.getColor(context, android.R.color.white)
            }

            val textInputEditText = TextInputEditText(context)
            textInputEditText.apply {

                layoutParams = LinearLayout.LayoutParams(

                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,

                    )
                setTextColor(ContextCompat.getColor(context, R.color.cement))
                setHintTextColor(ContextCompat.getColor(context, R.color.cement))
            }

            textInputEditText.hint = hint
            textInputLayout.addView(textInputEditText)
            editTextLinearLayout.addView(textInputLayout)
        }

        verticalLinearLayout.addView(editTextLinearLayout)


        val buttonsLinear =
            createLinearLayout(context, LinearLayout.HORIZONTAL, Gravity.END, right = 20)
        val b1 = createButton(
            context,
            capitalizeFirstLetter("save"),
            Gravity.CENTER,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            isAllCaps = false
        }
        val b2 = createButton(
            context,
            capitalizeFirstLetter("cancel"),
            Gravity.CENTER,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            isAllCaps = false
        }

        buttonsLinear.addView(b2)
        buttonsLinear.addView(b1)
        verticalLinearLayout.addView(buttonsLinear)

        dialog.setView(verticalLinearLayout)
        val alertDialog = dialog.create()
        alertDialog.show()
        b1.setOnClickListener {
            editTextLinearLayout.children.forEach {
                val text = (it as TextInputLayout).editText!!.text.toString()
                textFields.add(text)
            }
            obj.onClickYes(alertDialog, textFields)
        }
        b2.setOnClickListener {
            obj.onClickNo(alertDialog)
        }
    }

    private fun capitalizeFirstLetter(text: String): String {
        return text.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }


    fun createAlertDialog(
        context: Context,
        title: String,
        message: String,
        cancelable: Boolean = false
    ): AlertDialog.Builder {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        if (message.isNotEmpty()) {
            alertDialogBuilder.setMessage(message)
        }

        alertDialogBuilder.setCancelable(cancelable)
        return alertDialogBuilder
    }




    fun showProgressDialog(context: Context, message: String , cancelable: Boolean = true , transparent: Boolean = false , progressColor: Int =  R.color.gray50 ): Dialog {
        var progressDialog = Dialog(context)
        if (transparent) {
            progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setCancelable(cancelable)
        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        val progress = view.findViewById<ProgressBar>(R.id.progressBar)
        progress.indeterminateTintList = ColorStateList.valueOf(getColor(context,progressColor))
        messageTextView.text = message

        progressDialog.setContentView(view)
        progressDialog.show()
        return progressDialog
    }





}


