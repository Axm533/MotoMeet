package com.example.motomeet.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.example.motomeet.adapter.MeetingAdapter;
import com.example.motomeet.model.MeetingModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MeetingFragment extends Fragment {
    private List<MeetingModel> list;
    private ImageButton addEntryBtn, returnBtn;
    private RecyclerView recyclerView;
    private MeetingAdapter adapter;

    public MeetingFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meeting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        adapter = new MeetingAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);
        loadEntries();

        addEntryBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new AddMeetingFragment()));

        returnBtn.setOnClickListener(v -> startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class)));
    }

    public void init(View view){
        addEntryBtn = view.findViewById(R.id.addEntryBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
        list = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

    }

    private void loadEntries() {
        list.clear();

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Meetings");

        collectionReference.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

            if(error != null) return;

            if(value == null) return;

            list.clear();

            for(QueryDocumentSnapshot snapshot : value){
                if(!snapshot.exists()) return;

                MeetingModel model = snapshot.toObject(MeetingModel.class);

                list.add(model);

            }
            adapter.notifyDataSetChanged();

        });

    }
}
