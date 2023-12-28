package com.example.motomeet.fragments;

import static com.example.motomeet.fragments.CreateAccountFragment.EMAIL_REGEX;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motomeet.ReplacerActivity;
import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    private EditText emailEt, passwordEt;
    private TextView signUpTv, forgotPasswordTv;
    private Button signInBtn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        auth = FirebaseAuth.getInstance();

        clickListener();
    }
    private void init(View view){
        emailEt = view.findViewById(R.id.emailET);
        passwordEt = view.findViewById(R.id.passwordET);
        signInBtn = view.findViewById(R.id.signInBtn);
        signUpTv = view.findViewById(R.id.signUpTV);
        forgotPasswordTv = view.findViewById(R.id.forgotPassTV);
        progressBar = view.findViewById(R.id.progressBar);
    }
    private void clickListener(){

        signInBtn.setOnClickListener(v -> {

            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();

            if(email.isEmpty() || !email.matches(EMAIL_REGEX)){
                emailEt.setError("Invalid email");
                return;
            }

            if(password.isEmpty() || password.length() < 6){
                passwordEt.setError("Invalid password");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            if(!user.isEmailVerified()){
                                Toast.makeText(getContext(), "Verify your email", Toast.LENGTH_SHORT).show();
                            }

                            sendUserToMainActivity();
                        }else{
                            Toast.makeText(getContext(), "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        });

        signUpTv.setOnClickListener(v -> ((ReplacerActivity) getActivity()).setFragment(new CreateAccountFragment()));

        forgotPasswordTv.setOnClickListener(v -> ((ReplacerActivity) getActivity()).setFragment(new ForgotPasswordFragment()));
    }
    private void sendUserToMainActivity(){

        if(getActivity() == null)
            return;

        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }
}