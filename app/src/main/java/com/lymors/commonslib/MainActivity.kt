package com.lymors.commonslib


import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.lifecycleScope
import com.lymors.commonslib.MyUtils.dialogUtil
import com.lymors.commonslib.databinding.ActivityMainBinding
import com.lymors.commonslib.databinding.NewUserBinding
import com.lymors.commonslib.databinding.StudentSampleRowBinding
import com.lymors.lycommons.data.viewmodels.MainViewModel
import com.lymors.lycommons.data.viewmodels.StorageViewModel
import com.lymors.lycommons.extensions.ImageViewExtensions.loadImageFromUrl
import com.lymors.lycommons.extensions.ImageViewExtensions.pickImage
import com.lymors.lycommons.extensions.ScreenExtensions.myPermissionHelper
import com.lymors.lycommons.extensions.ScreenExtensions.pickedImageUri
import com.lymors.lycommons.extensions.TextEditTextExtensions.onTextChange
import com.lymors.lycommons.extensions.TextEditTextExtensions.setTextOrGone
import com.lymors.lycommons.extensions.ViewExtensions.attachDatePicker
import com.lymors.lycommons.extensions.ViewExtensions.setVisibleOrGone
import com.lymors.lycommons.extensions.ViewExtensions.setVisibleOrInvisible
import com.lymors.lycommons.utils.DialogUtil
import com.lymors.lycommons.utils.MyExtensions.hideSoftKeyboard
import com.lymors.lycommons.utils.MyExtensions.logT
import com.lymors.lycommons.utils.MyExtensions.setOptions
import com.lymors.lycommons.utils.MyExtensions.showSoftKeyboard
import com.lymors.lycommons.utils.MyExtensions.viewBinding
import com.lymors.lycommons.utils.MyPermissionHelper
import com.lymors.lycommons.utils.Utils.hideSoftKeyboard
import com.lymors.lycommons.utils.Utils.setData
import com.lymors.lycommons.utils.Utils.setTint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    // if you have to pick image in the dialog then use these two variables
    private lateinit var imageView: ImageView
    private var imageUri: Uri = Uri.EMPTY

    // if you want to implement long click logic then use these two lists
    var listOfSelectedViews = arrayListOf<View>()
    // change your model
    var listOfSelectedItems = arrayListOf<UserModel>() // if selecting items you will have them in this@MainActivity list now you can delete them

    lateinit var allUsers : List<UserModel>

    @Inject
    lateinit var mainViewModel: MainViewModel

    // to upload image to firebase storage
    @Inject
    lateinit var storageViewModel: StorageViewModel

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialogUtil.showProgressDialog(this,"Loading...")

        allUsers = listOf()
        lifecycleScope.launch {
            // change you model here
            mainViewModel.collectAnyModels("users" , UserModel::class.java , 10).collect { users ->
                users.logT("onCreate")
                allUsers = users
                setUpRecyclerView(allUsers.reversed(), 10)
            }

        }


        binding.floating.setTint(com.lymors.lycommons.R.color.yellow)



        setupBackButton()

        handleDeleteItems()
        handleUpdateItem()
        handleLongClickState()
        handleSearchState()

        myPermissionHelper.requestReadStoragePermission {

        }



        binding.searchIcon.pickImage(this){


        }

        binding.searchVew.onTextChange { query ->
            // filter by searchview
            var filteredList = allUsers.filter {it.name.contains(query, ignoreCase = true) }
            setUpRecyclerView(filteredList)
        }

//
//


        binding.floating.setOnClickListener {
            showNewUserDialog()
//            binding.recyclerview.convertToList(binding.sampleLinearLayout,allUsers).convertViewToPdf(this@MainActivity,"mango")

//            binding.recyclerview.convertRecyclerViewToPdf(this@MainActivity,"orange")
        }




        binding.searchIcon.setOnClickListener {
            mainViewModel.setSearchingState(true)
        }

     
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mainViewModel.searchingState.value){
            mainViewModel.setSearchingState(false)
        }
//            if you are in long click state come out of it
        else if (!mainViewModel.longClickedState.value){
            finishAffinity()
        }else{
            resetViews()
        }
    }

    private fun handleSearchState() {
//        change the right bottom button on the soft keyboard
        binding.searchVew.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Handle done action and close the keyboard
                hideSoftKeyboard()
                return@OnEditorActionListener true
            }
            false
        })

        lifecycleScope.launch {

            var permissionHelper = MyPermissionHelper(this@MainActivity)
            permissionHelper.requestReadStoragePermission{

            }

            // listen to the searching state
            mainViewModel.searchingState.collect { isSearching ->
                binding.apply {
                    // handle visibility of the views here
                    searchVew.setVisibleOrInvisible(!isSearching)
                    searchIcon.setVisibleOrGone(!isSearching)
                    title.setVisibleOrGone(!isSearching)
                    if (isSearching) {
                        pickedImageUri
                        searchVew.showSoftKeyboard()
                    } else {
                        binding.searchVew.setText("")
                        searchVew.hideSoftKeyboard()
                    }
                }
            }
        }

    }

    private fun handleLongClickState() {
        // listen to the long clicked state
        lifecycleScope.launch {
            mainViewModel.longClickedState.collect{
                binding.searchIcon.setVisibleOrInvisible(!it)
                binding.more.setVisibleOrGone(!it)
                binding.delete.setVisibleOrGone(it)
                binding.update.setVisibleOrGone(it)
            }
        }
    }

    private fun handleUpdateItem() {
        binding.update.setOnClickListener {
//            if you want to update item then also pass the item to update and title is optional
            showNewUserDialog("Update User", listOfSelectedItems[0])
        }
    }

    private fun handleDeleteItems() {
        binding.delete.setOnClickListener {
            // surety dialog
            dialogUtil.showInfoDialog(this,"Are you sure you want to delete selected items?","be care full your are gong to delete ${listOfSelectedItems.size} items","Delete","Cancel",false,object :DialogUtil.DialogClickListener{
                override fun onClickNo(d: DialogInterface) {
                    dialogUtil.dialog.dismiss()
                }

                override fun onClickYes(d: DialogInterface) {
                    lifecycleScope.launch {
                        // delete the selected items
                        listOfSelectedItems.forEach {
                            withContext(Dispatchers.Main){
                                val result = mainViewModel.deleteAnyModel("users/${it.key}")

                                result.showInToast(this@MainActivity)
                            }
                        }
                        resetViews()
                    }
                }

            })

        }
    }

    private fun setupBackButton() {
        // <- top left button in tool bar
        binding.back.setOnClickListener {
//            if (mainViewModel.searchingState.value){
//                mainViewModel.setSearchingState(false)
//            }
            if (mainViewModel.longClickedState.value) {
                resetViews()
            }

            else {
                this@MainActivity.onBackPressed()
            }
        }
    }

    private fun resetViews() {
        listOfSelectedViews.forEach {
            it.background = null
        }
        listOfSelectedViews.clear()
        listOfSelectedItems.clear()
        mainViewModel.setLongClickedState(false)
    }

    private fun setUpRecyclerView(users : List<UserModel> , pageSize : Int = 20) {



        binding.recyclerview.setData(users,null, pageSize ,  StudentSampleRowBinding::inflate , { b, item, position ->
            b.image1.loadImageFromUrl(item.profileImage)
            b.myname.text = item.name
            b.phone.text = item.phone
            b.feeNumber.text = position.toString()



            if (item in listOfSelectedItems){
                // if item is selected then set the background
                b.cardView.background = getDrawable(this@MainActivity, com.lymors.lycommons.R.drawable.selected_background)
            }else{
                b.cardView.background = null
            }

            b.cardView.setOnLongClickListener { view ->

                b.cardView.background = getDrawable(this@MainActivity, com.lymors.lycommons.R.drawable.selected_background)
                listOfSelectedViews.add(view)
                listOfSelectedItems.add(item)

                mainViewModel.setLongClickedState(true)
                true
            }


            b.cardView.setOnClickListener {

                if (mainViewModel.longClickedState.value) {
                    if (item in listOfSelectedItems) {
                        listOfSelectedItems.remove(item)
                        listOfSelectedViews.remove(it)
                        b.cardView.background = null
                        if (listOfSelectedItems.size == 0) {
                            mainViewModel.setLongClickedState(false)
                        }
                    } else {
                        listOfSelectedItems.add(item)
                        listOfSelectedViews.add(it)
                        b.cardView.background = getDrawable(this@MainActivity, com.lymors.lycommons.R.drawable.selected_background)
                    }
                    if (listOfSelectedItems.size==1){
                        binding.update.setVisibleOrGone(true)
                    }else{
                        binding.update.setVisibleOrGone(false)
                    }
                }else{
                    var intent = Intent(this@MainActivity , SecondActivity::class.java)
                    intent.putExtra("data","data")
                    startActivity(intent)
                }
            }
        },{ more->
            lifecycleScope.launch {
               var d =  dialogUtil.showProgressDialog(this@MainActivity , "Loading...")

                more.logT("more")
                // change you model here
                mainViewModel.collectAnyModels("users", UserModel::class.java, more ).collect { users ->
                    allUsers = users
                    users.logT("load more")
                    if (users.isNotEmpty()){
                    d.dismiss()
                    setUpRecyclerView( allUsers.reversed() , more)
                    }
                }
            }
        })

    }



    private fun showNewUserDialog(title:String = "", userModel: UserModel = UserModel()) {


        val dialogBinding = dialogUtil.showCustomLayoutDialog(this , NewUserBinding::inflate )

        dialogBinding.apply {
            dialogBinding.title.setTextOrGone(title)
            name.setText(userModel.name)
            phoneNumber.setText(userModel.phone)
            gender.setText(userModel.gender)
            birth.setText(userModel.birth)
            if (userModel.profileImage.isNotEmpty()){
                profileImage.loadImageFromUrl(userModel.profileImage)
            }

            birth.attachDatePicker()
            gender.setOptions(listOf("Male", "Female"))
            profileImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent)
            }
            smallImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent)
            }
            imageView = dialogBinding.profileImage
            cancelBtn.setOnClickListener { dialogUtil.dialog.dismiss() }
            saveBtn.setOnClickListener {
                resetViews()
                dialogUtil.dialog.dismiss()
                val name = dialogBinding.name.text.toString().trim()
                val phone = dialogBinding.phoneNumber.text.toString().trim()
                val gender = dialogBinding.gender.text.toString().trim()
                val birth = dialogBinding.birth.text.toString().trim()
                saveNewUser(UserModel(userModel.key, name, phone, gender, birth,""))
            }
        }
    }



    private fun saveNewUser(userModel: UserModel) {


        lifecycleScope.launch {
            if (imageUri != Uri.EMPTY) {
                val result = withContext(Dispatchers.IO){storageViewModel.uploadImageToFirebaseStorage(imageUri , "")}
                result.showInToast(this@MainActivity)
                result.whenSuccess {
                    lifecycleScope.launch {
                        userModel.profileImage = it
                        val result = mainViewModel.uploadAnyModel("users", userModel)
                        result.showInToast(this@MainActivity)
                    }
                }
                result.whenError {
                    "Can't upload your data to server: ${it.message}".logT()
                }
            } else {
                // when no image is picked
                lifecycleScope.launch {
                    val result = mainViewModel.uploadAnyModel("users", userModel)
                    result.showInToast(this@MainActivity)
                }
            }
        }
    }


//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        data?.data?.let {
//            showToast(it.toString())
//            imageView.setImageURI(it)
//            imageUri = it
//        }
//    }




    override fun onResume() {
        super.onResume()
        mainViewModel.setLongClickedState(false)
        mainViewModel.setSearchingState(false)
    }


    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            imageUri = data?.data!!
            imageView.setImageURI(imageUri)

        }
    }


}
