package com.education.godamy


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth



class GetStarted : AppCompatActivity() {
    private lateinit var auth :FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getstarted)

        auth = FirebaseAuth.getInstance()



        if(auth.currentUser != null ){
                if(intent.getBooleanExtra("completedSI",true)){
                startActivity(Intent(this,MainActivity::class.java))}
            else {
                findNavController(R.id.nav_host_first).navigate(R.id.action_signOptions_to_username2)
                }

        }
    }


}


