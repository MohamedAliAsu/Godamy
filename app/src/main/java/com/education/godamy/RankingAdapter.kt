package com.education.godamy

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.education.godamy.databinding.ModuleBinding
import com.education.godamy.databinding.UserRankingBinding

class UserRankingAdapter(private val userRankings: List<UserRanking>) :
    RecyclerView.Adapter<UserRankingAdapter.UserRankingViewHolder>() {
    class UserRankingViewHolder(val binding: UserRankingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rank: UserRanking) {
            val anim1 = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, binding.root.width.toFloat(), 0f)
            val anim2 = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
            ObjectAnimator.ofPropertyValuesHolder(binding.root,anim1,anim2).setDuration(400).start()

            binding.user = rank
            if(rank.username.contains("    (YOU)")){binding.root.background = AppCompatResources.getDrawable(binding.root.context!!,R.drawable.you_place)}
            when(rank.ranking){
                "#1" -> {binding.root.background = AppCompatResources.getDrawable(binding.root.context!!,R.drawable.first_place)}
                "#2" -> {binding.root.background = AppCompatResources.getDrawable(binding.root.context!!,R.drawable.second_place)}
                "#3" -> {binding.root.background = AppCompatResources.getDrawable(binding.root.context!!,R.drawable.third_place)}
            }

            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): UserRankingViewHolder {
                return UserRankingViewHolder(
                    UserRankingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRankingViewHolder {

        return UserRankingViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: UserRankingViewHolder, position: Int) {
        return holder.bind(userRankings[position])
    }

    override fun getItemCount(): Int {
        return userRankings.size
    }
}

data class UserRanking(
    val ranking: String,
    val username: String,
    val score: String
)




