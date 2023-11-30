package com.example.motomeet.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import com.example.motomeet.R;
import com.example.motomeet.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    SearchView searchView;
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    private List<UserModel> userList;
    OnDataPass onDataPass;
    CollectionReference collectionReference;

    public SearchFragment() {
        // Required empty public constructor
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        userAdapter.OnUserClicked(new UserAdapter.OnUserClicked() {
            @Override
            public void onClicked(String uid) {
                onDataPass.onChange(uid);
            }
        });
    }

    public interface OnDataPass {
        void onChange(String uid);
    }

    private void loadUserData() {

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null)
                    return;

                if (value == null)
                    return;

                userList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    UserModel users = snapshot.toObject(UserModel.class);
                    userList.add(users);
                }
                userAdapter.notifyDataSetChanged();
            }
        });
    }

    private void searchUser() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                collectionReference.orderBy("name").startAt(query).endAt(query + "\uf8ff")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {

                                    userList.clear();
                                    for (DocumentSnapshot snapshot : task.getResult()) {
                                        if (!snapshot.exists())
                                            return;

                                        UserModel users = snapshot.toObject(UserModel.class);
                                        userList.add(users);
                                    }
                                    userAdapter.notifyDataSetChanged();

                                }
                            }
                        });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.equals(""))
                    loadUserData();

                return false;
            }
        });
    }

}