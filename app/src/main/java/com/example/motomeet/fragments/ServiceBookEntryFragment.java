package com.example.motomeet.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceBookEntryFragment extends Fragment {
    private EditText serviceNameET, serviceDescriptionET, serviceCostET;
    private AppCompatButton addEntryBtn;

    private ImageButton returnBtn;
    private FirebaseUser user;
    private List<SwitchCompat> switchCompatList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_book_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        addEntryBtn.setOnClickListener(v -> uploadEntry());

        returnBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new ServiceBookFragment()));

    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        serviceNameET = view.findViewById(R.id.serviceNameET);
        serviceDescriptionET = view.findViewById(R.id.serviceDescriptionET);
        serviceCostET = view.findViewById(R.id.serviceCostET);
        addEntryBtn = view.findViewById(R.id.addEntryBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
        SwitchCompat oilSwitch = view.findViewById(R.id.oilSwitch);
        SwitchCompat oilFilterSwitch = view.findViewById(R.id.oilFilterSwitch);
        SwitchCompat sparkPlugsSwitch = view.findViewById(R.id.sparkPlugsSwitch);
        SwitchCompat driveUnitSwitch = view.findViewById(R.id.driveUnitSwitch);
        SwitchCompat valveClearanceSwitch = view.findViewById(R.id.valveClearanceSwitch);
        SwitchCompat breakFluidSwitch = view.findViewById(R.id.breakFluidSwitch);

        switchCompatList = new ArrayList<>();
        switchCompatList.add(oilSwitch);
        switchCompatList.add(oilFilterSwitch);
        switchCompatList.add(sparkPlugsSwitch);
        switchCompatList.add(driveUnitSwitch);
        switchCompatList.add(valveClearanceSwitch);
        switchCompatList.add(breakFluidSwitch);
    }

    private void uploadEntry() {

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("ServiceBook");

        String serviceName = serviceNameET.getText().toString();
        String serviceDescription = serviceDescriptionET.getText().toString();
        String serviceCost = serviceCostET.getText().toString();

        String id = collectionReference.document().getId();

        List<String> doneServices = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("uid", user.getUid());
        map.put("serviceName", serviceName);
        map.put("serviceDescription", serviceDescription);
        map.put("serviceCost", serviceCost);
        map.put("timestamp", FieldValue.serverTimestamp());

        for(SwitchCompat switchC : switchCompatList){
            if(switchC.isChecked())
                doneServices.add(switchC.getText().toString());
        }

        map.put("doneServices", TextUtils.join(", ", doneServices));

        collectionReference.document(id).set(map)
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Wpis dodany", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
                    ((MainActivity) getActivity()).switchFragment(new ServiceBookFragment());
        });

    }
}
