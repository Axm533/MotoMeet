package com.example.motomeet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.motomeet.fragments.CommentFragment;
import com.example.motomeet.fragments.CreateAccountFragment;
import com.example.motomeet.fragments.LoginFragment;

import org.w3c.dom.Comment;

public class ReplacerActivity extends AppCompatActivity {
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replacer);

        frameLayout = findViewById(R.id.frameLayout);

        boolean isComment = getIntent().getBooleanExtra("isComment", false);

        if (isComment)
            setFragment(new CommentFragment());
        else
            setFragment(new LoginFragment());
    }

    public void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if(fragment instanceof CreateAccountFragment){
            fragmentTransaction.addToBackStack(null);
        }

        if (fragment instanceof CommentFragment){

            String id = getIntent().getStringExtra("id");
            String uid = getIntent().getStringExtra("uid");

            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("uid", uid);
            fragment.setArguments(bundle);
        }

        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}