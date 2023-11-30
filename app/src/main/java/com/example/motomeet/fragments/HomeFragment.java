package com.example.motomeet.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.motomeet.R;
import com.example.motomeet.adapter.HomeAdapter;
import com.example.motomeet.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    public FirebaseUser user;
    DocumentReference documentReference;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        documentReference = FirebaseFirestore.getInstance().collection("Posts").document(user.getUid());
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getContext());
        recyclerView.setAdapter(adapter);

        loadDataFromFireStore();
    }

    private void init(View view){

        Toolbar toolbar = view.findViewById(R.id.toolbar);

        if(getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void loadDataFromFireStore() {

         //DocumentReference reference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());

         CollectionReference collectionReference = FirebaseFirestore.getInstance()
                 .collection("Users")
                 .document(user.getUid())
                 .collection("Post Images");

         collectionReference.addSnapshotListener((value, error) -> {

             if(error != null) {
                 Log.e("Error", error.getMessage());
                 return;
             }

             if (value == null)
                 return;

             //list.clear();

             for(QueryDocumentSnapshot snapshot : value){

                 if (!snapshot.exists())
                     return;

                 HomeModel model = snapshot.toObject(HomeModel.class);

                 list.add(new HomeModel(
                         model.getName(),
                         model.getProfileImage(),
                         model.getImageUrl(),
                         model.getUid(),
                         model.getDescription(),
                         model.getId(),
                         model.getTimestamp(),
                         model.getLikes()));
             }
             adapter.notifyDataSetChanged();
         });

    }
}