package com.example.motomeet.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import com.example.motomeet.R;
import com.example.motomeet.adapter.UserAdapter;
import com.example.motomeet.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    SearchView searchView;
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    private List<UserModel> userList;
    OnDataPass onDataPass;
    CollectionReference collectionReference;

    public SearchFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onDataPass = (OnDataPass) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        collectionReference = FirebaseFirestore.getInstance().collection("Users");

        loadUserData();
        searchUser();
        clickListener();
    }

    private void init(View view) {
        searchView = view.findViewById(R.id.searchView);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);
    }

    private void clickListener() {
        userAdapter.OnUserClicked(uid -> onDataPass.onChange(uid));
    }

    public interface OnDataPass {
        void onChange(String uid);
    }

    private void loadUserData() {
        collectionReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            UserModel users = snapshot.toObject(UserModel.class);
                            if (!users.getUid().equals(user.getUid())) {
                                userList.add(users);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }else{
                        Log.d("SearchFragment", "Błąd ładowania dokumentów: ", task.getException());
                    }
                });
    }

    private void searchUser() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadUserData();
                } else {
                    performSearch(newText);
                }
                return false;
            }
        });
    }

    private void performSearch(String query) {
        collectionReference.orderBy("name").startAt(query).endAt(query + "\uf8ff").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            UserModel users = snapshot.toObject(UserModel.class);
                            if (!users.getUid().equals(user.getUid())) {
                                userList.add(users);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("SearchFragment", "Błąd ładowania dokumentów: ", task.getException());
                    }
                });
    }

}