package com.education.godamy

import androidx.core.util.Pair
import java.util.Collections.emptyList

data class User(val Username :String, val Fullname:String, var quizzesScores : List<Pair<String,Int>> =emptyList<Pair<String,Int>>())
