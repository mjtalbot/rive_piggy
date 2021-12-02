package com.test.piggybank

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.rive.runtime.kotlin.core.Rive

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Rive.init(applicationContext)
        setContentView(R.layout.activity_main)
    }
}