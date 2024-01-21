package com.education.godamy

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.animation.doOnEnd
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.education.godamy.databinding.FragmentResultBinding
import com.education.godamy.repository.GodamyDatabase
import com.education.godamy.repository.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var binding :FragmentResultBinding
    val rankings = mutableListOf<UserRanking>()
    private var User =MutableLiveData (User("","",""))
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_result,container,false)
        val score = arguments?.getInt("score")
        val path = arguments?.getString("path")
        val maxGrade = arguments?.getInt("maxGrade")
        val username = arguments?.getString("username")
        db = Firebase.firestore
        binding.scoree.text = score?.toString() +" / "+ maxGrade?.toString()

        if((score!!.toFloat()/maxGrade!!.toFloat()) >= 0.5f) {
            binding.el7afla.start(Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 60,
                angle = Angle.RIGHT ,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                position = Position.Relative(0.0, 0.0),
                emitter = Emitter(duration = 2700, TimeUnit.MILLISECONDS).max(100)
            ))
            binding.el7afla2.start(Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 60,
                angle = Angle.LEFT ,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                position = Position.Relative(1.0, 0.0),
                emitter = Emitter(duration = 2700, TimeUnit.MILLISECONDS).max(100)
            ))
        }

        val anim = ObjectAnimator.ofFloat(binding.cardView,"translationX",-1.2f,0f).setDuration(1700)
        binding.scoree.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.uptodown))
        binding.yourScoree.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.righttoleft))
        anim.doOnEnd { binding.goMain.visibility = VISIBLE
            binding.goMain.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.fadein))
        }
        anim.start()
        binding.goMain.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_mainFragment)
        }




                val currentUserRanking = UserRanking("#1",username!!,score.toString())

                val userRankForFS = hashMapOf(
                    "username" to currentUserRanking.username,
                    "score" to score

                )
                val userRef = db.collection("users").document(currentUserRanking.username)
                val userQuizRef = userRef.collection("quizzes").document(path!!.replace("/", "_"))
//                userRef.apply {
//

//                    collection("quizzes").document(path!!.replace("/", "_"))
//                    .set(hashMapOf(
//                        "score" to score
//                    ))}

                userQuizRef.get().addOnCompleteListener {
                    if(it.isSuccessful){
                        val quizDoc = it.result

                        if(quizDoc.exists()){

                            val diff = score?.toLong()!! - quizDoc.getLong("score")!!
                            userRef.update("totalScore",FieldValue.increment(diff))
                            lifecycleScope.launch { withContext(IO) {
                                val userOld = GodamyDatabase.getInstance(requireContext()).dao.getuser()
                                userOld.totalScore +=diff.toInt()
                                GodamyDatabase.getInstance(requireContext()).dao.signIn(userOld)
                            } }

                        }else{
                            userRef.update("totalScore" ,FieldValue.increment(score!!.toLong()!!))
                            lifecycleScope.launch { withContext(IO) {
                                val userOld = GodamyDatabase.getInstance(requireContext()).dao.getuser()
                                userOld.totalScore +=score
                                GodamyDatabase.getInstance(requireContext()).dao.signIn(userOld)
                            } }


                        }
                    }
                    userQuizRef.set(hashMapOf("score" to score))
                }
                db.collection("QuizzesScores").document(path!!.replace("/", "_")).collection("users").document(currentUserRanking.username).set(userRankForFS)
                db.collection("QuizzesScores").document(path!!.replace("/", "_")).collection("users")
                    .orderBy("score",Query.Direction.DESCENDING).get() .addOnSuccessListener {
                        var i = 1
                        var pos = 0
                       for(doc in it ){
                           if(doc.getString("username")!!.contains(currentUserRanking.username)){
                               rankings.add(UserRanking("#$i",
                                   doc.getString("username")!! + "    (YOU)",
                                   doc.getLong("score")?.toString()!!))
                               pos = i-1
                           }else {
                               rankings.add(
                                   UserRanking(
                                       "#$i",
                                       doc.getString("username")!!,
                                       doc.getLong("score")?.toString()!!
                                   )
                               )
                           }
                           i++
                       }
                        requireActivity().runOnUiThread {
                            binding.rankings.adapter = UserRankingAdapter(rankings)
                            binding.rankings.scrollToPosition(pos)
                        }

                }.addOnFailureListener {

                }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){

        }
    }
}