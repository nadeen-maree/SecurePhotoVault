package com.example.securephotovault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private Animation topAnim,bottomAnim;
    private TextView logo_txt,motto;
    private ImageView logo_img;
    private static int DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logo_txt = findViewById(R.id.logo_txt);
        motto = findViewById(R.id.motto);
        logo_img = findViewById(R.id.logo_img);

        logo_txt.setAnimation(bottomAnim);
        motto.setAnimation(bottomAnim);
        logo_img.setAnimation(topAnim);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this,LockScreenActivity.class);
            startActivity(intent);
            finish();
        },DELAY);

    }
}