package com.lymors.lycommons.utils


import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.lymors.lycommons.R

class DialogUtil( val context: Activity):Dialog(context) {

    lateinit var dialog:Dialog

    interface DialogClickListener{
        fun onClickYes(d: DialogInterface)
        fun onClickNo(d: DialogInterface)
    }

    interface EditTextDialogClickListener{
        fun onClickYes(d: DialogInterface , texts:ArrayList<String>)
        fun onClickNo(d: DialogInterface)
    }


    fun showAlertDialogRounded( title: String, message: String, cancelable: Boolean = false ) {
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
    }

    inline fun <reified T : ViewBinding> showCustomLayoutDialog(
        crossinline bindingInflater: (LayoutInflater) -> T
    ): T {
        val binding = bindingInflater.invoke(layoutInflater)
        dialog = AlertDialog.Builder(context).setView(binding.root).show()
        return binding
    }


    fun showAlertDialog( title: String, message: String, cancelable: Boolean = false ) {
        val alertDialogBuilder = createAlertDialog(title, message, cancelable)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    fun showInfoDialog( title: String, message: String, positiveButtonLabel:String = "Ok", negativeButtonLabel:String = "Cancel", cancelable: Boolean, obj: DialogClickListener) {
        val alertDialogBuilder = createAlertDialog(title, message, cancelable)
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
        title: String,
        hints: List<String>,
        obj: EditTextDialogClickListener
    ) {
        val textFields = ArrayList<String>()
        val dialog = createAlertDialog("", "")
        val verticalLinearLayout = createLinearLayout(LinearLayout.VERTICAL, Gravity.CENTER )

        // Set title
        val titleTextView = createTextView(title, Gravity.CENTER_HORIZONTAL, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT , 30f,0,25,0,10,true)
        titleTextView.setTextColor(ContextCompat.getColor(context, R.color.cement))
        verticalLinearLayout.addView(titleTextView)

        // Set all TextInputLayouts
        val editTextLinearLayout = createLinearLayout(LinearLayout.VERTICAL, Gravity.CENTER)
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
                boxBackgroundColor = ContextCompat.getColor(context, R.color.very_very_light_cement)
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


        var buttonsLinear = createLinearLayout(LinearLayout.HORIZONTAL,Gravity.END , right = 20)
        var b1 = createButton("Save",Gravity.CENTER,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
        var b2 = createButton("Cancel",Gravity.CENTER,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)

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





    fun createLinearLayout(orientation: Int = LinearLayout.VERTICAL, gravity: Int = Gravity.CENTER,left:Int = 15,top:Int=15,right:Int=15,bottom:Int=15): LinearLayout {
        val linearLayout = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(left,top,right,bottom)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = orientation
        linearLayout.gravity = gravity
        return linearLayout
    }


    fun createButton(
        text: String,
        gravity: Int,
        width: Int,
        height: Int,
        textColorRes: Int = R.color.cement  // Color resource for the text color
    ): Button {
        val button = Button(context)
        val layoutParams = LinearLayout.LayoutParams(width, height)
        button.layoutParams = layoutParams
        button.gravity = gravity
        button.text = text
        button.setTextColor(ContextCompat.getColor(context, textColorRes))  // Set text color
        return button
    }



    fun createTextView(
        text: String,
        gravity: Int,
        width: Int,
        height: Int,
        textSize: Float = 16f,  // Default text size in sp
        marginLeft: Int = 0,   // Default margin
        marginTop: Int = 0,
        marginRight: Int = 0,
        marginBottom: Int = 0,
        isBold: Boolean = false  // Default not bold
    ): TextView {
        val textView = TextView(context)

        val layoutParams = LinearLayout.LayoutParams(width, height)
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
        textView.layoutParams = layoutParams

        textView.gravity = gravity
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

        if (isBold) {
            textView.setTypeface(null, Typeface.BOLD)  // Set typeface to bold
        }

        return textView
    }



    fun createAlertDialog(title: String, message: String , cancelable:Boolean= false):AlertDialog.Builder{
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        if (message.isNotEmpty()){
            alertDialogBuilder.setMessage(message)
        }

        alertDialogBuilder.setCancelable(cancelable)
        return alertDialogBuilder
    }




    fun showProgressDialog(context: Context, message: String): Dialog {
        val progressDialog = Dialog(context)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.setCancelable(false)

        val progressBar = ProgressBar(context)
        val messageTextView = TextView(context)
        messageTextView.text = message

        progressDialog.setContentView(progressBar)
        progressDialog.show()

        return progressDialog
    }



}