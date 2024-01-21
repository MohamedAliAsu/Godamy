package com.education.godamy.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GodamyDao {

    @Query("SELECT * FROM user LIMIT 1")
    fun getuser(): User

    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Insert(onConflict =  OnConflictStrategy.REPLACE  )
    fun signIn( user: User)

    @Query("Delete From user")
    fun signOut()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPath(path:Paths)


}