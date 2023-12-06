package com.example.motomeet.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountFragment extends Fragment {

    private EditText nameEt, emailEt, passwordEt, confirmPasswordEt;
    private TextView loginTv;
    private Button signUpBtn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    public static final String EMAIL_REGEX = "^(.+)@(.+)$";

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        clickListener();
    }

    private void init(View view) {
        nameEt = view.findViewById(R.id.nameET);
        emailEt = view.findViewById(R.id.emailET);
        passwordEt = view.findViewById(R.id.passwordET);
        confirmPasswordEt = view.findViewById(R.id.confirmPasswordET);
        loginTv = view.findViewById(R.id.loginTV);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        progressBar = view.findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
    }

    private void clickListener() {

        loginTv.setOnClickListener(v -> ((ReplacerActivity) getActivity()).setFragment(new LoginFragment()));

        signUpBtn.setOnClickListener(v -> {
            String name = nameEt.getText().toString();
            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();
            String confirmPassword = confirmPasswordEt.getText().toString();

            if (name.isEmpty() || name.equals(" ")){
                nameEt.setError("Name invalid");
                return;
            }

            if (email.isEmpty() || !email.matches(EMAIL_REGEX)){
                nameEt.setError("Email invalid");
                return;
            }

            if (password.isEmpty() || password.length() < 6){
                nameEt.setError("Password invalid");
                return;
            }

            if (confirmPassword.isEmpty() || !confirmPassword.equals(password)){
                nameEt.setError("Confirm your password");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            createAccount(name, email, password);
        });
    }

    private void uploadUser(FirebaseUser user , String name, String email){

        List<String> followersList = new ArrayList<>();
        List<String> followingList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("email", email);
        map.put("profileImage", "");
        map.put("uid", user.getUid());
        map.put("following", followingList);
        map.put("followers", followersList);
        map.put("status", " ");


        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            assert getActivity() != null;
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(getContext().getApplicationContext(), MainActivity.class));
                            getActivity().finish();
                        }else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createAccount(String name, String email, String password){

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            FirebaseUser user = auth.getCurrentUser();

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();
                            //request.setPhotoUri(Uri.parse(image));

                            user.updateProfile(request);

                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getContext(), "Email verification link send", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            uploadUser(user, name, email);

                        }else{
                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG,"createUserWithEmail:failure");
                            Toast.makeText(getContext(), "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}