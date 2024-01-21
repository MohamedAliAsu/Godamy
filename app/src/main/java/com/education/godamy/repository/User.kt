package com.education.godamy.repository

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.education.godamy.User
import org.apache.commons.lang3.mutable.Mutable

@Entity(tableName = "user")
data class User(
    val uid:String,
    @PrimaryKey var username:String,
    var fullname : String,
    var currentPath:String = "",

    var totalScore:Int = 0 ) {

}