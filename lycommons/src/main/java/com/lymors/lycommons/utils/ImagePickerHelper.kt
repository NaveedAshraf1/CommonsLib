package com.lymors.lycommons.utils

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.lymors.lycommons.utils.MyExtensions.toUri


class MediaPickerHelper(private val activity: AppCompatActivity) {
    var permissionHelper = MyPermissionHelper(activity)
    // Define callbacks
   private var onMediaPicked: ((Uri?) -> Unit)? = null
   private var onImagesPicked: ((List<Uri>?) -> Unit)? = null


    // Pick single image
    private val pickImageLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onMediaPicked?.invoke(uri)
        }

    // Pick multiple images
    private val pickImagesLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            onImagesPicked?.invoke(uris)
        }

    // Pick audio
    private val pickAudioLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onMediaPicked?.invoke(uri)
        }

    // Pick video
    private val pickVideoLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onMediaPicked?.invoke(uri)
        }

    // Pick document
    private val pickDocumentLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onMediaPicked?.invoke(uri)
        }


    fun pickImage(callback: (Uri?) -> Unit) {
        onMediaPicked = callback
        pickImageLauncher.launch("image/*")
    }

    fun pickImages(callback: (List<Uri>?) -> Unit) {
        permissionHelper.requestReadStoragePermission{
            if (it){
                onImagesPicked = callback
                pickImagesLauncher.launch("image/*")
            }
        }
    }

    fun pickAudio(callback: (Uri?) -> Unit) {
        onMediaPicked = callback
        pickAudioLauncher.launch("audio/*")
    }

    fun pickVideo(callback: (Uri?) -> Unit) {
        permissionHelper.requestReadStoragePermission{
            if (it){
                onMediaPicked = callback
                pickVideoLauncher.launch("video/*")
            }
        }

    }

    fun pickDocument(callback: (Uri?) -> Unit) {
        permissionHelper.requestReadStoragePermission{
            if (it){
                onMediaPicked = callback
                pickDocumentLauncher.launch("*/*")
            }
        }

    }


    fun pickCroppedImage(callback: (Uri?) -> Unit) {
        onMediaPicked = callback
        pickCroppedImageLauncher.launch(null)
    }



//    fun pickCroppedImages(callback: (List<Uri>?) -> Unit) {
//        onImagesPicked = callback
//        pickCroppedImagesLauncher.launch(null)
//    }

    // Pick single image with crop
    private val pickCroppedImageLauncher: ActivityResultLauncher<Void?> =
        activity.registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            onMediaPicked?.invoke(bitmap?.toUri(activity))
        }





}
