package com.education.godamy.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paths")
data class Paths(
    @PrimaryKey(autoGenerate = true)var id :Int =0,
    val path :String
    )