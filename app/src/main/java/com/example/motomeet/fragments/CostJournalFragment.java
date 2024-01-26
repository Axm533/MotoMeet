package com.example.motomeet.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.motomeet.adapter.CostJournalAdapter;
import com.example.motomeet.model.CostJournalModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CostJournalFragment extends Fragment {

    private List<CostJournalModel> list;

    CostJournalAdapter adapter;

    RecyclerView recyclerView;

    private ImageButton addEntryBtn, returnBtn;

    private FirebaseUser user;

    public CostJournalFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cost_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        adapter = new CostJournalAdapter(list);
        recyclerView.setAdapter(adapter);
        loadEntries();

        addEntryBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new CostJournalEntryFragment()));

        returnBtn.setOnClickListener(v -> startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class)));
    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        list = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerViewCostJournal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        addEntryBtn = view.findViewById(R.id.addEntryBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
    }

    private void loadEntries() {
        list.clear();

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("CostJournal");

        collectionReference.orderBy("entryDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

            if(error != null) return;

            if(value == null) return;

            list.clear();

            for(QueryDocumentSnapshot snapshot : value){
                if(!snapshot.exists()) return;

                CostJournalModel model = snapshot.toObject(CostJournalModel.class);

                list.add(model);

            }
            adapter.notifyDataSetChanged();

        });

    }
}
