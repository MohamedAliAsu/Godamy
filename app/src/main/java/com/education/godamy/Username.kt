package com.education.godamy


import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.education.godamy.databinding.FragmentUsernameBinding
import com.education.godamy.repository.GodamyDatabase
import com.education.godamy.repository.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Username : Fragment() {


    private lateinit var binding: FragmentUsernameBinding
    private lateinit var fs: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_username, container, false)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        fs = Firebase.firestore

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rootH = binding.root.height
            val screenHeight = binding.root.context.resources.displayMetrics.heightPixels

            val heightDiff = screenHeight - rootH

            if (heightDiff > screenHeight * 0.15) {
                // Keyboard is shown
                // You can take appropriate action here
                if(!binding.username.isFocused){
                binding.eun.visibility = GONE
                binding.username.visibility = GONE}

            } else {
                binding.eun.visibility = VISIBLE
                binding.username.visibility = VISIBLE

                // Keyboard is hidden
                // You can take appropriate action here
            }
            binding.root.requestLayout()
        }
        binding.username.imeOptions = EditorInfo.IME_ACTION_NEXT
        binding.username.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_NEXT) {
                binding.fullName.requestFocus()
               return@setOnEditorActionListener true
            }else {
                return@setOnEditorActionListener false
            }

        }

        binding.nextBtn.setOnClickListener {
            if (TextUtils.isEmpty(binding.username.text)) {
                Snackbar.make(binding.root, "Please enter a username !", Snackbar.LENGTH_SHORT)
                    .show()
            } else if (TextUtils.isEmpty(binding.fullName.text)) {
                Snackbar.make(
                    binding.root,
                    "Please enter Your FullName (First and Last name)",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                val username = binding.username.text.toString()
                val fullname = binding.fullName.text.toString()

                fs.collection("users").document(username).get().addOnSuccessListener {
                    if (it.exists()) {
                        binding.usedUsername.visibility = VISIBLE
                    } else {
                        binding.usedUsername.visibility = INVISIBLE
                        it.reference.set(
                            hashMapOf(
                                "username" to username,
                                "fullName" to fullname,
                                "totalScore" to 0,
                                "EmailOrPhone" to (currentUser?.email ?: currentUser?.phoneNumber!!)
                            )
                        )

                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                GodamyDatabase.getInstance(requireContext()).dao.signIn(
                                    User(
                                        currentUser?.uid!!,
                                        username,
                                        fullname
                                    )
                                )
                                startActivity(Intent(requireActivity(), MainActivity::class.java))
                            }

                        }
                    }

                }
            }
        }
        return binding.root
    }




}