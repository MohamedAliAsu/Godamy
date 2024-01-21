package com.education.godamy

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.godamy.databinding.FragmentPhoneBinding
import com.education.godamy.repository.GodamyDatabase
import com.education.godamy.repository.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class Phone : Fragment() {
    private lateinit var binding:FragmentPhoneBinding
    private lateinit var verificationId:String
    private lateinit var auth : FirebaseAuth
    private lateinit var progressDialog:ProgressDialog
    private lateinit var fs : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_phone,container,false)
        auth = FirebaseAuth.getInstance()
        fs = Firebase.firestore
        binding.modify.setOnClickListener {
            binding.verificationSection.visibility = View.INVISIBLE
            binding.submitPhone.visibility =View.VISIBLE
            binding.enterPhoneText.visibility = View.VISIBLE
            binding.verificationSection.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.fadeout))
            binding.submitPhone.isEnabled = true
            binding.phone.isEnabled = true

        }

        binding.submitPhone.setOnClickListener {
            if(TextUtils.isEmpty(binding.phone.text)||(binding.phone.text.length != 11)){
                Snackbar.make(binding.root,"Please Enter a Valid Phone Number",Snackbar.LENGTH_SHORT).show()
            }else{

                showProgressDialog()
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                }, 5000)
                val phoneNumber = "+2" + binding.phone.text.toString()
                val pops = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)
                           .setActivity(requireActivity())
                           .setTimeout(60,TimeUnit.SECONDS)
                           .setCallbacks(
                        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                progressDialog.dismiss()
                                // Auto-retrieval or instant validation has worked
                                auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        findNavController().navigate(R.id.action_phone2_to_username2)

                                    }
                                }
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                progressDialog.dismiss()
                                // Verification failed
                                Snackbar.make(binding.root,"Sorry, An Error Occurred!",Snackbar.LENGTH_SHORT).show()

                            }

                            override fun onCodeSent(
                                verificationId: String,
                                token: PhoneAuthProvider.ForceResendingToken
                            ) {
                                progressDialog.dismiss()
                                this@Phone.verificationId = verificationId

                                // The SMS verification code has been sent to the provided phone number
                                binding.verificationSection.visibility = View.VISIBLE
                                binding.submitPhone.visibility =View.INVISIBLE
                                binding.enterPhoneText.visibility = View.INVISIBLE
                                binding.verificationSection.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.fadein))
                                binding.submitPhone.isEnabled = false
                                binding.phone.isEnabled = false


                            }

                        }
                    ).build()
                PhoneAuthProvider.verifyPhoneNumber(pops)

        }

    binding.verifiyCode.setOnClickListener {
        if(TextUtils.isEmpty(binding.verifiyCode.text)){ Snackbar.make(binding.root,"Enter the verification code sent to you!",Snackbar.LENGTH_SHORT).show() }

        else{
            val verificationCode = binding.code.text.toString()
                                val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
                                auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        val query = fs.collection("users").whereEqualTo("EmailOrPhone",("+2${binding.phone.text}"))
                                        query.get().addOnCompleteListener {querySnapshot ->
                                            if(querySnapshot.isSuccessful && !querySnapshot.result.isEmpty){

                                                if(querySnapshot.result.documents.size > 0) {
                                                    querySnapshot.result.documents.forEach {

                                                        lifecycleScope.launch {
                                                            withContext(Dispatchers.IO){
                                                                GodamyDatabase.getInstance(requireContext()).dao.signIn(
                                                                    User(
                                                                        "godamyDammy",
                                                                        it.getString("username")!!,
                                                                        it.getString("fullName")!!,
                                                                        totalScore = it.getLong("totalScore")!!.toInt()
                                                                    )
                                                                )}}

                                                    }
                                                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                                                }



                                            }else {
                                                findNavController().navigate(R.id.action_phone2_to_username2)
                                            }

                                        }


                                    }
                                    else {
                                        Snackbar.make(binding.root,"Wrong Code, please enter the right code",200).show()
                                    }
                                }
        }
    }

    }
        return binding.root


}


    private fun showProgressDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog.show()
        progressDialog.setContentView(R.layout.loading)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

    }
}