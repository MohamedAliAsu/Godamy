package com.education.godamy

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.education.godamy.databinding.ModuleBinding

class ModulesAdabter(val moduleClick: ModuleClick) : ListAdapter<String , ModulesAdabter.ModuleVH>(diff) {

    class ModuleVH (val bind : ModuleBinding): RecyclerView.ViewHolder(bind.root){
        fun bind(module: String, c: ModuleClick) {
            val anim1 = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, bind.root.width.toFloat(), 0f)
            val anim2 = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
            ObjectAnimator.ofPropertyValuesHolder(bind.root,anim1,anim2).setDuration(400).start()

            bind.module = module
            bind.click = c
            bind.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ModuleVH {
                return ModuleVH(ModuleBinding.inflate(LayoutInflater.from(parent.context),
                    parent,
                    false))
            }
        }
    }
    object diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleVH {
        return ModuleVH.from(parent)
    }

    override fun onBindViewHolder(holder: ModuleVH, position: Int) {
        holder.bind(getItem(position),moduleClick )
    }
}

class ModuleClick(val module: (String) -> Unit) {

    fun onClick(module: String) = module(module)
}
