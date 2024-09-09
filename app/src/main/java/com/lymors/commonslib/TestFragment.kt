
package com.lymors.commonslib


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.lymors.commonslib.databinding.FragmentTestBinding
import com.lymors.commonslib.databinding.NewUserBinding
import com.lymors.lycommons.data.viewmodels.MainViewModel
import com.lymors.lycommons.extensions.ImageViewExtensions.loadImageFromUrl
import com.lymors.lycommons.extensions.ScreenExtensions.pickedImageUri
import com.lymors.lycommons.extensions.ScreenExtensions.showToast
import com.lymors.lycommons.extensions.ScreenExtensions.viewBinding
import com.lymors.lycommons.extensions.TextEditTextExtensions.onTextChange
import com.lymors.lycommons.extensions.ViewExtensions.attachDatePicker
import com.lymors.lycommons.extensions.ViewExtensions.setVisibleOrGone
import com.lymors.lycommons.extensions.ViewExtensions.setVisibleOrInvisible
import com.lymors.lycommons.utils.DialogUtil
import com.lymors.lycommons.utils.MyExtensions.hideSoftKeyboard
import com.lymors.lycommons.utils.MyExtensions.logT
import com.lymors.lycommons.utils.MyExtensions.setOptions
import com.lymors.lycommons.utils.MyExtensions.showSoftKeyboard
import com.lymors.lycommons.utils.Utils.hideSoftKeyboard
import com.lymors.lycommons.utils.Utils.showCustomLayoutDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class TestFragment : Fragment() {

    private var listOfSelectedViews = arrayListOf<View>()
    // change your model
    var listOfSelectedItems = arrayListOf<UserModel>() // if selecting items you will have them in this@MainActivity list now you can delete them

    private var allUsers : List<UserModel> = listOf()

    @Inject
    lateinit var mainViewModel: MainViewModel
    lateinit var imagePicker : ImageView



    private val binding by viewBinding(FragmentTestBinding::inflate)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        imagePicker = ImageView(requireActivity())
        "onCreate".logT()

        lifecycleScope.launch {
            mainViewModel.collectAnyModels("users" , UserModel::class.java , 10).collect { users ->
                "lodtop--size".logT(users.size.toString())
                showToast(users.size.toString())
                allUsers = users
                setUpRecyclerView(allUsers.reversed(), 10)
            }
        }


        setupBackButton()

        handleDeleteItems()
        handleUpdateItem()
        handleLongClickState()
        handleSearchState()




        binding.searchVew.onTextChange { query ->
            // filter by searchview
            var filteredList = allUsers.filter {it.name.contains(query, ignoreCase = true) }
            setUpRecyclerView(filteredList)
        }

//
//


        binding.floating.setOnClickListener {



//            val dialog = CustomDialogFragment()
//            dialog.show(supportFragmentManager, "CustomDialogFragment")
            showNewUserDialog()
//            binding.recyclerview.convertToList(binding.sampleLinearLayout,allUsers).convertViewToPdf(this@MainActivity,"mango")

//            binding.recyclerview.convertRecyclerViewToPdf(this@MainActivity,"orange")
        }




        binding.searchIcon.setOnClickListener {
            mainViewModel.setSearchingState(true)
        }

return binding.root
    }

//    override fun onBackPressed() {
//
//        if (mainViewModel.searchingState.value){
//            mainViewModel.setSearchingState(false)
//        }
//        else if (mainViewModel.longClickedState.value){
//            resetViews()
//        }else{
//            super.onBackPressed()
//            finishAffinity()
//        }
//    }

    private fun handleSearchState() {
//        change the right bottom button on the soft keyboard
        binding.searchVew.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Handle done action and close the keyboard
               requireActivity(). hideSoftKeyboard()
                return@OnEditorActionListener true
            }
            false
        })

        lifecycleScope.launch {

            // listen to the searching state
            mainViewModel.searchingState.collect { isSearching ->
                binding.apply {
                    // handle visibility of the views here
                    searchVew.setVisibleOrInvisible(isSearching)
                    searchIcon.setVisibleOrGone(!isSearching)
                    title.setVisibleOrGone(!isSearching)
                    if (isSearching) {
                       requireActivity(). pickedImageUri
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
//        binding.delete.showInfoDialog("Are you sure you want to delete selected items?","be care full your are gong to delete ${listOfSelectedItems.size} items","Delete","Cancel",false,object :DialogUtil.DialogClickListener{
//                override fun onClickNo(d: DialogInterface) {
//                    d.dismiss()
//                }
//
//                override fun onClickYes(d: DialogInterface) {
//                    lifecycleScope.launch {
//                        // delete the selected items
//                        listOfSelectedItems.forEach {
//                            withContext(Dispatchers.Main){
//                                val result = mainViewModel.deleteAnyModel("users/${it.key}")
//
//                                result.showInToast(requireActivity())
//                            }
//                        }
//                        resetViews()
//                    }
//                }
//
//            })

    }

    private fun setupBackButton() {
        // <- top left button in tool bar
        binding.back.setOnClickListener {
            if (mainViewModel.searchingState.value){
                mainViewModel.setSearchingState(false)
            }
            else if (mainViewModel.longClickedState.value) {
                resetViews()
            }
            else {
                requireActivity().onBackPressed()
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



//        binding.recyclerview.setData(users,null, pageSize ,  StudentSampleRowBinding::inflate , { b, item, position ->
//
//            b.profileImage.loadImageFromUrl(item.profileImage)
//            b.name.text = item.name
//            b.phone.text = item.phone
//            b.birth.text = position.toString()
//
//            if (item in listOfSelectedItems){
//                // if item is selected then set the background
//                b.cardView.background = getDrawable(requireActivity(), com.lymors.lycommons.R.drawable.selected_background)
//            }else{
//                b.cardView.background = null
//            }
//
//            b.cardView.setOnLongClickListener { view ->
//
//                b.cardView.background = getDrawable(requireActivity(), com.lymors.lycommons.R.drawable.selected_background)
//                listOfSelectedViews.add(view)
//                listOfSelectedItems.add(item)
//
//                mainViewModel.setLongClickedState(true)
//                true
//            }
//
//            b.cardView.setOnClickListener {
//
//                if (mainViewModel.longClickedState.value) {
//                    if (item in listOfSelectedItems) {
//                        listOfSelectedItems.remove(item)
//                        listOfSelectedViews.remove(it)
//                        b.cardView.background = null
//                        if (listOfSelectedItems.size == 0) {
//                            mainViewModel.setLongClickedState(false)
//                        }
//                    } else {
//                        listOfSelectedItems.add(item)
//                        listOfSelectedViews.add(it)
//                        b.cardView.background = getDrawable(requireActivity(), com.lymors.lycommons.R.drawable.selected_background)
//                    }
//                    if (listOfSelectedItems.size==1){
//                        binding.update.setVisibleOrGone(true)
//                    }else{
//                        binding.update.setVisibleOrGone(false)
//                    }
//                }else{
//                    var intent = Intent(requireActivity() , SecondActivity::class.java)
//                    intent.putExtra("data","data")
//                    startActivity(intent)
//                }
//            }
//        },{ more->
//            lifecycleScope.launch {
//                var d =  dialogUtil.showProgressDialog(requireActivity() , "Loading...")
//
//                more.logT("more")
//                // change you model here
//                mainViewModel.collectAnyModels("users", UserModel::class.java, more ).collect { users ->
//                    allUsers = users
//                    users.logT("load more")
//                    if (users.isNotEmpty()){
//                        d.dismiss()
//                        setUpRecyclerView( allUsers.reversed() , more)
//                    }
//                }
//            }
//        })

    }

    private fun showNewUserDialog(title:String = "", userModel: UserModel = UserModel()) {

        showCustomLayoutDialogFragment(requireActivity(), NewUserBinding::inflate) { dialogBinding, dialogFragment ->
            dialogBinding.apply {
//                title.setTextOrGone(title)
                name.setText(userModel.name)
                phoneNumber.setText(userModel.phone)
                gender.setText(userModel.gender)
                birth.setText(userModel.birth)
                if (userModel.profileImage.isNotEmpty()){
                    profileImage.loadImageFromUrl(userModel.profileImage)
                }

                birth.attachDatePicker()
                gender.setOptions(listOf("Male", "Female"))
//                profileImage.pickImageMagic(this@MainActivity){
//                    showToast(it.toString())
//                }

//                profileImage.pickImageInDialog {
//                    profileImage.setImageURI(requireActivity().pickedImageUri)
//                }

                cancelBtn.setOnClickListener { dialogFragment.dismiss() }
                saveBtn.setOnClickListener {

                    resetViews()
                    dialogFragment.dismiss()

                    val name = dialogBinding.name.text.toString().trim()
                    val phone = dialogBinding.phoneNumber.text.toString().trim()
                    val gender = dialogBinding.gender.text.toString().trim()
                    val birth = dialogBinding.birth.text.toString().trim()
                    var u = UserModel(userModel.key, name, phone, gender, birth,"")

                    lifecycleScope.launch {
                        mainViewModel.uploadModelWithImage(requireActivity() , "users", u, requireActivity().pickedImageUri.toString(),UserModel::profileImage)
                    }
                }
            }
            dialogBinding.cancelBtn.setOnClickListener {
                showToast("cancel button pressed")
                dialogFragment.dismiss()
            }

            // Handle save action
        }



//        dialogUtil.showCustomLayoutDialog(this , NewUserBinding::inflate ){ dBinding , dialog ->
//            dBinding.apply {
////                title.setTextOrGone(title)
//                name.setText(userModel.name)
//                phoneNumber.setText(userModel.phone)
//                gender.setText(userModel.gender)
//                birth.setText(userModel.birth)
//                if (userModel.profileImage.isNotEmpty()){
//                    profileImage.loadImageFromUrl(userModel.profileImage)
//                }
//
//                birth.attachDatePicker()
//                gender.setOptions(listOf("Male", "Female"))
////                profileImage.pickImageMagic(this@MainActivity){
////                    showToast(it.toString())
////                }
//
//                profileImage.pickImage {
//                  profileImage.setImageURI(it)
//                }
//
//
//
//                cancelBtn.setOnClickListener { dialogUtil.dialog.dismiss() }
//                saveBtn.setOnClickListener {
//
//                    resetViews()
//                    dialogUtil.dialog.dismiss()
//
//                    val name = dBinding.name.text.toString().trim()
//                    val phone = dBinding.phoneNumber.text.toString().trim()
//                    val gender = dBinding.gender.text.toString().trim()
//                    val birth = dBinding.birth.text.toString().trim()
//                    var u = UserModel(userModel.key, name, phone, gender, birth,"")
////                        myPermissionHelper.requestReadStoragePermission {
////                            showToast("storage permission granted")
////                            if (it){
////                    lifecycleScope.launch {
//////                        mainViewModel.uploadModelWithImage(this@MainActivity , "users", u, pickedImageUri.toString(),UserModel::profileImage)
////                            }
////                        }else{
////                            showToast("storage permission is required to upload image")
////                            }
////                    }
//                }
//            }
//
//        }


    }

    override fun onResume() {
        "onResume".logT()
        super.onResume()
        mainViewModel.setLongClickedState(false)
        mainViewModel.setSearchingState(false)
    }

    override fun onStart() {
        "onStart".logT()
        super.onStart()
        mainViewModel.setLongClickedState(false)
        mainViewModel.setSearchingState(false)
    }

    override fun onPause() {
        super.onPause()
        "onPause".logT()
    }


}





// Inside a separate file (ImagePickerViewModel.kt)
//object ImagePickerViewModel{
//
//
//    val AppCompatActivity.resultLauncher
//    get() = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val pickedUri = result.data?.data ?: Uri.EMPTY
//            pickedImageUri = pickedUri
//            onImagePicked?.invoke(pickedUri) // Call the callback with the URI
//        }
//    }
//
//    private var onImagePicked: ((Uri?) -> Unit)? = null
//
//    fun View.pickImageMagic(activity: AppCompatActivity , callback: (Uri?) -> Unit) {
//        onImagePicked = callback
//        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
//        activity.resultLauncher.launch(intent)
//    }
//}
