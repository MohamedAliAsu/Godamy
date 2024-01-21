package com.education.godamy

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.godamy.databinding.FragmentSignOptionsBinding
import com.education.godamy.repository.GodamyDatabase
import com.education.godamy.repository.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SignOptions : Fragment() {


    private lateinit var binding: FragmentSignOptionsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var fs: FirebaseFirestore
    private val RC_SIGN_IN = 1
    private lateinit var progDialog:ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sign_options, container, false)
        auth = FirebaseAuth.getInstance()
        fs = Firebase.firestore

        progDialog = ProgressDialog(requireContext())
        progDialog.setMessage("Logging you in")
        progDialog.setTitle("Please Wait")
        binding.buttons.visibility = View.INVISIBLE
        val logoAnimation = AnimationUtils.loadAnimation(this.context, R.anim.zoomin)
        binding.logo.startAnimation(logoAnimation)
        binding.logo.postDelayed({
            binding.logo.startAnimation(
                AnimationUtils.loadAnimation(
                    this.context,
                    R.anim.logo_animation
                )
            )
            binding.buttons.startAnimation(
                AnimationUtils.loadAnimation(
                    this.context,
                    R.anim.fadein
                )
            )
            binding.buttons.visibility = View.VISIBLE
        }, 1000)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()
        gsc = GoogleSignIn.getClient(this.requireActivity(), gso)
        binding.withGoogle.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.click))
            it.postDelayed({
                it.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.click2))

                startActivityForResult(gsc.signInIntent, RC_SIGN_IN)
                progDialog.show()

            }, 170)


        }

        binding.withPhone.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.click))
            it.postDelayed({
                it.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.click2))
                findNavController().navigate(R.id.action_signOptions_to_phone22)

            }, 170)
        }
        return binding.root

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {


                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

                firebaseAuth(account)
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error Occured, try again", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun firebaseAuth(googleaccount: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(googleaccount.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { authRes ->
            binding.buttons.isEnabled = false
            if (authRes.isSuccessful) {
                val query = fs.collection("users").whereEqualTo("EmailOrPhone", googleaccount.email)
                query.get().addOnCompleteListener { querySnapshot ->
                    if (querySnapshot.isSuccessful && !querySnapshot.result.isEmpty) {


                            querySnapshot.result.documents.forEach {
                                if (querySnapshot.result.documents.size > 0) {
                                lifecycleScope.launch {
                                withContext(Dispatchers.IO) {

                                        GodamyDatabase.getInstance(requireContext()).dao.signIn(
                                            User(
                                                googleaccount.id!!,
                                                it.getString("username")!!,
                                                it.getString("fullName")!!,
                                                totalScore = it.getLong("totalScore")!!.toInt()
                                            )
                                        )
                                    }
                                }

                                    progDialog.dismiss()
                                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                            }else {
                                    progDialog.dismiss()
                                    findNavController().navigate(R.id.action_signOptions_to_username2)
                                }
                        }

                    }else {
                        progDialog.dismiss()
                        findNavController().navigate(R.id.action_signOptions_to_username2)
                    }

                }

            } else {

                Snackbar.make(binding.root, "Sorry, a problem occurred", Snackbar.LENGTH_SHORT)
                    .show()
            }

        }
    }


}