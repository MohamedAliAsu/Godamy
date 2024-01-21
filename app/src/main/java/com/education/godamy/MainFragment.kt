package com.education.godamy

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.godamy.databinding.FragmentMainBinding
import com.education.godamy.databinding.PasswordDialogBinding
import com.education.godamy.repository.GodamyDatabase
import com.education.godamy.repository.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Scanner
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var storage: FirebaseStorage

    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ModulesAdabter
    private var list: List<String> = emptyList()
    private var path = ""
    private var user: MutableLiveData<User> = MutableLiveData(User("", "", ""))
    private lateinit var dialog: Dialog
    private lateinit var sp : SharedPreferences
    var password = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.lifecycleOwner = this

        sp = requireActivity().getSharedPreferences("pathPref", Context.MODE_PRIVATE)
        path = sp.getString("path","")!!

        try {


        storage = Firebase.storage
        auth = FirebaseAuth.getInstance()



        binding.logout.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle("Sign Out, are you sure?")
                .setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    lifecycleScope.launch {
                        withContext(IO) {
                            try {
                                GodamyDatabase.getInstance(requireContext()).dao.signOut()

                            } catch (_: Exception) {

                            }
                        }

                    }
                    auth.signOut()
                    startActivity(Intent(requireActivity(), GetStarted::class.java))

                }.setNegativeButton("No") { ff, sss ->
                    ff.dismiss()
                }.show()

        }

        adapter = ModulesAdabter(ModuleClick { folderClicked ->
            path = "$path/$folderClicked"
            headToPath()
        })
        binding.modulesRecycler.adapter = adapter
        binding.modulesRecycler.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(), R.anim.rv_enter
            )
        )

        binding.swipeRefresh.isRefreshing = true
        user.observe(this.viewLifecycleOwner) {

            binding.apply {

                try {


                    welcome.text = getString(R.string.hi, it.username)
                    score.text = it.totalScore.toString()
                } catch (_: Exception) {
                }
            }
        }
        Timer().schedule(1200){
            if(binding.welcome.text == getString(R.string.hi,"")){
                startActivity(Intent(requireContext(),GetStarted::class.java).putExtra("completedSI",false))
            }
        }
        headToPath()
        try {

                    val userFromDb = GodamyDatabase.getInstance(requireContext()).dao.getuser()
                    user.postValue(userFromDb)


        } catch (_: Exception) {

        }

        binding.swipeRefresh.setOnRefreshListener {

            headToPath()
        }}catch (_:Exception){

        }

        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            path = path.substringBeforeLast("/")
            sp.edit().putString("path",path).apply()
            headToPath()
        }

    }

    fun headToPath() {


        if (path == "") {
            storage.reference.listAll().addOnSuccessListener {
                list = it.prefixes.map {
                    it.name
                }

                binding.swipeRefresh.isRefreshing = false
                adapter.submitList(list)

            }.addOnFailureListener {
                Snackbar.make(
                    binding.root, "Weak or No Internet Connection ", Snackbar.LENGTH_SHORT
                ).show()

            }
        } else {
            storage.reference.child(path).listAll().addOnSuccessListener {
                list = it.prefixes.map {
                    it.name
                }
                var pass = false
                var csv = false
                binding.swipeRefresh.isRefreshing = false
                if (it.prefixes.isEmpty()) {

                    it.items.forEach { ref ->
                        if (ref.name.contains(".txt")) {
                            pass = true
                            try {
                                val localFile = File.createTempFile("password", "txt")

                                ref.getFile(localFile).addOnSuccessListener {
                                    val scanner = Scanner(localFile)
                                    if (scanner.hasNext()) {
                                        password = scanner.next()
                                    }
//                                                requireActivity().runOnUiThread {
//                                                    binding.modulesRecycler.isEnabled = false
//                                                }

                                    requestPassword(password)
                                    scanner.close()
                                }


                            } catch (e: Exception) {
                                // Handle the case where the file is not found
                            }

                        } else if (ref.name.contains(".csv")) {
                            csv = true
                        }
                    }
                    if (csv && !pass) {
                        sp.edit().putString("path",path.substringBeforeLast("/")).apply()
                        findNavController().navigate(
                            R.id.action_mainFragment_to_questionsFragment,
                            bundleOf("quizPath" to path)
                        )
                    }
                }
                if (!pass && !csv) {
                    sp.edit().putString("path",path).apply()
                    adapter.submitList(list)
                }
            }.addOnFailureListener {
                Snackbar.make(
                    binding.root, "Weak or No Internet Connection ", Snackbar.LENGTH_SHORT
                ).show()
            }
        }


    }

    fun requestPassword(password: String) {
        val dialogBind = DataBindingUtil.inflate<PasswordDialogBinding>(
            LayoutInflater.from(requireContext()), R.layout.password_dialog, null, false
        )
        dialog = Dialog(requireContext())
        dialog.setContentView(dialogBind.root)
        dialogBind.check.setOnClickListener {

            if (dialogBind.password.text.toString().equals(password)) {
                dialog.dismiss()
                sp.edit().putString("path",path.substringBeforeLast("/")).apply()
                findNavController().navigate(
                    R.id.action_mainFragment_to_questionsFragment, bundleOf("quizPath" to path)
                )

            } else {
                val sb = Snackbar.make(binding.root, "Wrong Password", Snackbar.LENGTH_SHORT)
                sb.view.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(), androidx.appcompat.R.color.error_color_material_light
                    )
                )

                path = path.substringBeforeLast("/")
                sp.edit().putString("path",path).apply()
                dialog.dismiss()
                sb.show()
            }
        }
        dialogBind.cancel.setOnClickListener {
            path = path.substringBeforeLast("/")
            sp.edit().putString("path",path).apply()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

    }




}