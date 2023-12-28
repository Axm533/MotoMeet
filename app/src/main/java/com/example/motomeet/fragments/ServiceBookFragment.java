package com.example.motomeet.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.example.motomeet.adapter.ServiceBookAdapter;
import com.example.motomeet.model.ServiceBookModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ServiceBookFragment extends Fragment {

    private List<ServiceBookModel> entriesList;

    ServiceBookAdapter adapter;
    RecyclerView recyclerView;
    private ImageButton addEntryBtn, returnBtn;

    private FirebaseUser user;

    public ServiceBookFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_servicebook, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        adapter = new ServiceBookAdapter(entriesList);
        recyclerView.setAdapter(adapter);
        loadEntries();

        addEntryBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new ServiceBookEntryFragment()));

        returnBtn.setOnClickListener(v -> startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class)));
    }

    private void init(View view) {

        addEntryBtn = view.findViewById(R.id.addEntryBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
        entriesList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerViewServiceBook);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    private void loadEntries() {

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("ServiceBook");

        collectionReference.addSnapshotListener((value, error) -> {

            if(error != null) {
                Log.d("loadEntries error: ", error.getMessage());
                return;
            }

            if(value == null)
                return;

            for(QueryDocumentSnapshot snapshot : value){
                if(!snapshot.exists())
                    return;

                ServiceBookModel model = snapshot.toObject(ServiceBookModel.class);

                entriesList.add(new ServiceBookModel(
                        model.getServiceName(),
                        model.getServiceDescription(),
                        model.getServiceCost(),
                        model.getDoneServices(),
                        model.getId(),
                        model.getUid()));

            }
            adapter.notifyDataSetChanged();

        });

    }
}
