package com.example.tripplan.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.R
import android.widget.ImageView
import com.bumptech.glide.Glide

class SplashHomeActivity : AppCompatActivity() {
    private lateinit var gifImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_home)

        gifImageView = findViewById(R.id.gif_suitcase)
        Glide.with(this)
            .asGif()
            .load(R.drawable.suitcase) // drawable 폴더에 있는 GIF 파일
            .into(gifImageView)

    }

    override fun onStop() {
        super.onStop()
        Glide.with(this).clear(gifImageView)
    }
}