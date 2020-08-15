package com.kp.FoodFlash.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.kp.FoodFlash.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler().postDelayed({
            val start = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(start)
            finishAffinity()
        }, 2000)
        title = ""
    }


}