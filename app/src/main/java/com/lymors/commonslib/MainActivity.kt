package com.lymors.commonslib

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.lymors.commonslib.databinding.ActivityMainBinding
import com.lymors.lycommons.data.models.SampleModel
import com.lymors.lycommons.data.viewmodels.MainViewModel
import com.lymors.lycommons.extensions.ContextExtensions.showToast
import com.lymors.lycommons.extensions.ScreenExtensions.pickedImageUri
import com.lymors.lycommons.utils.*
import com.lymors.lycommons.utils.MyExtensions.viewBinding
import com.lymors.lycommons.utils.MyImagePicker.pickImageByGallery
import com.lymors.lycommons.utils.MyImagePicker.registerActivityForImageLauncher
import com.lymors.lycommons.utils.MyResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var mainViewModel: MainViewModel

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        registerActivityForImageLauncher()

        binding.floating.setOnClickListener {
            showToast("Opening upload dialog...")
            showUploadDialog()
        }

        lifecycleScope.launch {
            mainViewModel.collectAnyModels("sampleModels", SampleModel::class.java).collect { models ->
//                binding.statusTextView.text = "Uploaded models: ${models.size}\nFirst Item: ${models.firstOrNull()}"

            }
        }
    }

    private fun showUploadDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_upload, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)
        val uploadButton = view.findViewById<Button>(R.id.uploadButton)

        imageButton.setOnClickListener {
            imageButton.pickImageByGallery(this@MainActivity) { uri ->
                pickedImageUri = uri
                showToast("Image selected")
                // Optionally, load the image into the imageButton
                // Glide.with(this@MainActivity).load(uri).into(imageButton)
            }
        }

        uploadButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty() || pickedImageUri == null) {
                showToast("Please enter a name and select an image")
                return@setOnClickListener
            }

            val model = SampleModel(name = name)
            val realTimePath = "sampleModels"
            val imageUris = listOf(pickedImageUri.toString())
            val properties = listOf(SampleModel::imageUrl as KProperty<*>)

            lifecycleScope.launch {
                val result = mainViewModel.uploadModelWithImages(this@MainActivity, realTimePath, model, imageUris, properties)
                when (result) {
                    is MyResult.Success -> {
                        showToast("Upload successful!")
                        dialog.dismiss()
                    }
                    is MyResult.Error -> {
                        showToast("Upload failed: ${result.message}")
                    }
                }
            }
        }

        dialog.show()
    }
}