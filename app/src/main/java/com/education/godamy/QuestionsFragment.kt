package com.education.godamy

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.education.godamy.databinding.ExitQuizBinding
import com.education.godamy.databinding.FragmentQuestionsBinding
import com.education.godamy.repository.GodamyDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

class QuestionsFragment : Fragment() {
    private lateinit var binding: FragmentQuestionsBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var answersBtnsList: List<TextView>
    private var answersList = listOf<String>()
    private var rightAnswer = ""
    private lateinit var path: String
    var i = 0

    private var questionsList = mutableListOf<Question>()
    private var score = 0
    private var audioQuestion = false
    private var audioQuestionName = ""
    private var audioAnswers = false
    private var audioAnswer = ""
    private var audioFiles = mutableMapOf<String, File>()
    private lateinit var mp: MediaPlayer
    private lateinit var dialogBind: ExitQuizBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_questions, container, false)
        answersBtnsList = listOf(binding.q1, binding.q2, binding.q3, binding.q4)
        path = arguments?.getString("quizPath", "")!!
        storage = Firebase.storage

        mp = MediaPlayer()

        storage.reference.child(path).listAll().addOnSuccessListener { listResult ->
                    listResult.items.forEach {
                        if (it.name.contains(".csv")) {
                            val csvRef = it
                            val localFile = File.createTempFile("questionsCsv", "csv")

                            csvRef.getFile(localFile).addOnSuccessListener {
                                val whole_document = CSVReader(FileReader(localFile)).readAll()
                                whole_document.forEach { row ->
                                    questionsList.add(
                                        Question(
                                            row[0], row[1], row[2], row[3], row[4], row[5]
                                        )
                                    )
                                    if (questionsList.size == 1) {

                                            binding.loading.visibility = GONE
                                            binding.mainLO.visibility = VISIBLE


                                    }
                                }


                                //questionsList.shuffle()
                                i += 1
                                activity?.runOnUiThread {
                                    binding.progressBar2.max = (questionsList.size - 1) * 50
                                    binding.progText.text = "$i/${questionsList.size}"
                                }

                                nextQuestion()
                            }
                        }
                        if (it.name.contains(".mp3")) {
                            val audioRef = it
                            val localFile = File.createTempFile("${it.name}", "mp3")


                            audioRef.getFile(localFile).addOnSuccessListener { success ->
                                audioFiles["$path/${it.name}"] = localFile
                            }
                        }
                    }


                }



        binding.nextQuestion.setOnClickListener {
            binding.videoQuestion.pause()
            if ((it as Button).text == getString(R.string.submitAudio)) {
                it.text = getString(R.string.next_question)
            }
            if (audioAnswers) {
                answersBtnsList.forEach {
                    it.isEnabled = false
                }
                if (audioAnswer == rightAnswer) {

                    answersBtnsList.forEach {
                        if (it.tag.toString() == rightAnswer) {
                            it.background = getDrawable(requireContext(), R.drawable.choice_correct)
                            it.startAnimation(
                                AnimationUtils.loadAnimation(
                                    requireContext(),
                                    R.anim.right_answer
                                )
                            )
                        }
                    }
                    playAudio("correct")
                    score++
                } else {
                    answersBtnsList.forEach {
                        if (it.tag.toString() == rightAnswer) {
                            it.background = getDrawable(requireContext(), R.drawable.choice_correct)
                        } else if (it.tag.toString() == audioAnswer) {
                            it.background = getDrawable(requireContext(), R.drawable.choice_wrong)
                            it.startAnimation(
                                AnimationUtils.loadAnimation(
                                    requireContext(),
                                    R.anim.wrong_answer
                                )
                            )
                        }
                    }

                    playAudio("wrong")
                }

                audioAnswers = false
                return@setOnClickListener
            }

            answersBtnsList.forEach {
                it.isEnabled = true
                it.background = getDrawable(requireContext(), R.drawable.choice_before)
            }

            nextQuestion()
        }
        binding.audioOrText.setOnClickListener {
            if (audioQuestion) {
                playAudio("$path/$audioQuestionName")
            }
        }

        binding.videoQuestion.apply {
            setOnClickListener {
                if(isPlaying) pause() else try{start()}catch (_:Exception){}
            }
            setOnCompletionListener {
                it.seekTo(0)
                it.start()
            }

            setOnPreparedListener{
                it.start()
            }
        }

        answersBtnsList.forEach {

            it.setOnClickListener { answer ->
                if (audioAnswers) {

                    answer.background = getDrawable(requireContext(), R.drawable.choice_listen)
                    answersBtnsList.minus(answer).forEach {

                        it.background = getDrawable(requireContext(), R.drawable.choice_before)
                    }
                    audioAnswer = it.tag.toString()
                    playAudio("$path/${it.tag}")
                    return@setOnClickListener
                } else {
                    if (it.text == rightAnswer) {
                        it.background = getDrawable(requireContext(), R.drawable.choice_correct)
                        it.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.right_answer
                            )
                        )
                        playAudio("correct")
                        score += 1
                    } else {
                        it.background = getDrawable(requireContext(), R.drawable.choice_wrong)
                        it.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.wrong_answer
                            )
                        )
                        playAudio("wrong")
                    }
                    answersBtnsList.forEach {
                        it.isEnabled = false
                        if (it.text == rightAnswer) {
                            it.background = getDrawable(requireContext(), R.drawable.choice_correct)


                        }
                    }
                }
                binding.nextQuestion.isEnabled = true
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogBind = ExitQuizBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog =
            AlertDialog.Builder(requireContext()).setView(dialogBind.root).setCancelable(true)
                .create()
        dialogBind.ja.setOnClickListener {
            findNavController().navigate(R.id.action_questionsFragment_to_mainFragment)
            dialog.dismiss()
        }
        dialogBind.nein.setOnClickListener {
            dialog.dismiss()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            dialog.show()
        }
    }

    fun nextQuestion() {

        if (i == questionsList.size - 1) {
            binding.nextQuestion.text = "Finish"
        }

        if ((i < (questionsList.size))) {


            answersList = listOf(
                questionsList[i].Correct,
                questionsList[i].wrong1,
                questionsList[i].wrong2,
                questionsList[i].wrong3
            ).shuffled()
            if (questionsList[i].q.contains(".mp3")) {
                audioQuestion = true
                binding.audioOrText.text = "Click to listen \uD83D\uDD0A"
                audioQuestionName = questionsList[i].q
            } else {
                audioQuestion = false
            }
            if (answersList[0].contains(".mp3")) {
                audioAnswers = true
                binding.nextQuestion.text = getString(R.string.submitAudio)
                answersBtnsList.zip(answersList) { btn, ans ->
                    btn.text = "\uD83D\uDD0A"
                    btn.tag = ans
                }
                rightAnswer = questionsList[i].Correct
                binding.apply {
                    questionPart1.text = questionsList[i].qp1
                    imageQuestion.visibility = GONE
                    videoQuestion.visibility = GONE
                    loadingImg.visibility = GONE
                    audioOrText.visibility = VISIBLE

                }
                i += 1
                ObjectAnimator.ofInt(binding.progressBar2, "progress", ((i - 1) * 50))
                    .setDuration(350)
                    .start()
                binding.progressBar2.progress += (i - 1) * 50
                binding.progText.text = "${(i - 1)}/${(questionsList.size - 1)}"
                return
            } else {
                audioAnswers = false
            }

            binding.apply {
                nextQuestion.isEnabled = false
                questionPart1.text = questionsList[i].qp1
                if (questionsList[i].q.contains(".jpg") || questionsList[i].q.contains(".png")) {
                    audioOrText.visibility = GONE
                    imageQuestion.visibility = GONE
                    videoQuestion.visibility = GONE
                    loadingImg.visibility = VISIBLE
                    storage.reference.child("$path/${questionsList[i].q}").downloadUrl.addOnSuccessListener {
                        loadingImg.visibility = GONE
                        imageQuestion.visibility = VISIBLE
                        Glide.with(this@QuestionsFragment).load(it).into(imageQuestion)

                    }

                }
                else if(questionsList[i].q.contains(".mp4")){

                    audioOrText.visibility = GONE
                    imageQuestion.visibility = GONE
                    videoQuestion.visibility = GONE
                    loadingImg.visibility = VISIBLE
                    storage.reference.child("$path/${questionsList[i].q}").downloadUrl.addOnSuccessListener {

                        loadingImg.visibility = GONE

                        videoQuestion.setVideoURI(it!!)
                        videoQuestion.visibility = VISIBLE

                    }
                }


                else {
                    imageQuestion.visibility = GONE
                    loadingImg.visibility = GONE
                    audioOrText.visibility = VISIBLE
                    videoQuestion.visibility = GONE
                    audioOrText.text =
                        if (questionsList[i].q.contains(".mp3")) "Click to listen \uD83D\uDD0A" else questionsList[i].q
                }
            }
            rightAnswer = questionsList[i].Correct
            answersBtnsList.zip(answersList) { btn, ans ->
                if (ans == "") {
                    btn.visibility = GONE
                } else {
                    btn.visibility = VISIBLE
                    btn.text = ans
                }

            }

            i += 1
            ObjectAnimator.ofInt(binding.progressBar2, "progress", ((i - 1) * 50)).setDuration(350)
                .start()
            binding.progressBar2.progress += (i - 1) * 50
            binding.progText.text = "${(i - 1)}/${questionsList.size - 1}"

        } else {
            playAudio("win")
            lifecycleScope.launch {
                mp.release()
                withContext(Dispatchers.IO) {
                    val username =
                        GodamyDatabase.getInstance(requireContext()).dao.getuser().username
                    requireActivity().runOnUiThread {
                        findNavController().navigate(
                            R.id.action_questionsFragment_to_resultFragment,
                            bundleOf(
                                "score" to score,
                                "path" to path,
                                "maxGrade" to (questionsList.size - 1),
                                "username" to username
                            )

                        )
                    }
                }
            }


        }
    }


    private fun playAudio(audioPath: String) {

        if(audioPath == "wrong"){
            MediaPlayer.create(requireContext(),R.raw.wrong).start()
            return
        }else if(audioPath == "correct"){
            MediaPlayer.create(requireContext(),R.raw.correct).start()
            return
        }else if(audioPath == "win"){
            MediaPlayer.create(requireContext(),R.raw.win).start()
            return
        }

        try {


            if (audioFiles.containsKey(audioPath)) {


                if(mp.isPlaying){
                    mp.stop()
                    mp.reset()
                }
                mp.reset()
                mp.setDataSource(audioFiles[audioPath]?.absolutePath)
                mp.setOnPreparedListener {
                    it.start()
                }
                mp.prepareAsync()



            }
        } catch (_: Exception) {
        }
    }


}