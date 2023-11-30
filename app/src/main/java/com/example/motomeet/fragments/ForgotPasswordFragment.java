package com.example.motomeet.fragments;

import static com.example.motomeet.fragments.CreateAccountFragment.EMAIL_REGEX;

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
import com.example.motomeet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private TextView returnToLoginTv;
    private Button recoverBtn;
    private EditText emailEt;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListener();
    }

    private void init(View view) {

        returnToLoginTv = view.findViewById(R.id.returnToLoginTV);
        recoverBtn = view.findViewById(R.id.recoverBtn);
        emailEt = view.findViewById(R.id.emailET);
        progressBar = view.findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

    }

    private void clickListener(){

        returnToLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });

        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEt.getText().toString();

                if(email.isEmpty() || !email.matches(EMAIL_REGEX)){
                    emailEt.setError("Invalid email");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(), "Email sent",
                                            Toast.LENGTH_SHORT).show();

                                    emailEt.setText("");
                                }else{
                                    Toast.makeText(getContext(), "Error: "+task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        });

            }
        });

    }
}