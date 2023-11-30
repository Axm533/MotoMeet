package com.example.motomeet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.motomeet.fragments.LoginFragment;
import com.example.motomeet.ReplacerActivity;
import com.google.android.gms.common.util.concurrent.HandlerExecutor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        final FirebaseUser user = auth.getCurrentUser();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                /*if(user == null){
                    startActivity(new Intent(SplashActivity.this, ReplacerActivity.class));
                }else{
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }*/
                startActivity(new Intent(SplashActivity.this, ReplacerActivity.class));
                finish();
            }
        }, 2500);
    }
}