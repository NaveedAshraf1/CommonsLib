package com.lymors.lycommons.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding

class GenericDialogFragment<B : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> B,
    private val setupBinding: (B, DialogFragment) -> Unit
) : DialogFragment() {

    fun show(manager: FragmentManager) {
        show(manager, tag)
    }

    fun dismissDialog() {
        dismiss()
    }

    private var _binding: B? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater.invoke(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBinding(binding, this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
