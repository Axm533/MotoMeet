package com.example.motomeet.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CostJournalEntryFragment extends Fragment {

    private EditText fuelCostET, highwayCostET, additionalCostET;

    private CalendarView entryDateCV;

    private Button addEntryBtn, returnBtn;

    private FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cost_journal_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        final String[] entryDate = new String[1];

        entryDateCV.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            entryDate[0] = dayOfMonth + "." + month + "." + year;
        });

        addEntryBtn.setOnClickListener(v -> uploadEntry(entryDate[0]));

        returnBtn.setOnClickListener(v -> startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class)));
    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        fuelCostET = view.findViewById(R.id.fuelCostET);
        highwayCostET = view.findViewById(R.id.highwayCostET);
        additionalCostET = view.findViewById(R.id.additionalCostET);
        entryDateCV = view.findViewById(R.id.entryDateCV);
        addEntryBtn = view.findViewById(R.id.addEntryBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
    }

    private void uploadEntry(String entryDate) {

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("CostJournal");

        String fuelCost = fuelCostET.getText().toString();
        String highwayCost = highwayCostET.getText().toString();
        String additionalCost = additionalCostET.getText().toString();

        String id = collectionReference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("fuelCost", fuelCost);
        map.put("highwayCost", highwayCost);
        map.put("additionalCost", additionalCost);
        map.put("entryDate", entryDate);
        map.put("uid", user.getUid());

        collectionReference.document(id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Entry added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    ((MainActivity) getActivity()).switchFragment(new CostJournalFragment());
                });

    }
}