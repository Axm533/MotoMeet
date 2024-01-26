package com.example.motomeet.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.motomeet.BuildConfig;
import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddMeetingFragment extends Fragment implements OnMapReadyCallback {

    private EditText meetingNameET, meetingDescriptionET, meetingDateET;
    private AppCompatButton addEntryBtn;
    private ImageButton returnBtn;
    private FirebaseUser user;
    private GoogleMap mMap;
    private Double latitude, longitude;

    private float currentZoom;

    String apiKey = BuildConfig.API_KEY;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_meeting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapMeeting);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        init(view);
        addEntryBtn.setOnClickListener(v -> uploadEntry());

        returnBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new MeetingFragment()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnCameraMoveListener(() -> {
            currentZoom = mMap.getCameraPosition().zoom;
            Log.d("Map Zoom Level", "Current Zoom: " + currentZoom);
        });

        mMap.setOnMapClickListener(latLng -> {
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(latLng.latitude+" : "+latLng.longitude));
        });
    }

    private void init(View view){
        user = FirebaseAuth.getInstance().getCurrentUser();
        addEntryBtn = view.findViewById(R.id.addEntryBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
        meetingNameET = view.findViewById(R.id.meetingNameET);
        meetingDescriptionET = view.findViewById(R.id.meetingDescriptionET);
        meetingDateET =  view.findViewById(R.id.meetingDateET);
    }

    public void uploadEntry(){

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Meetings");

        String meetingName = meetingNameET.getText().toString();
        String meetingDescription = meetingDescriptionET.getText().toString();
        String meetingDate = meetingDateET.getText().toString();

        String id = collectionReference.document().getId();

        String imageUrl = "https://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude
                + "&zoom="+currentZoom+"&size=200x200&sensor=false&markers=" + latitude + "," + longitude + "&key="+apiKey;

        Log.d("Static Map URL", "URL: " + imageUrl);
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("uid", user.getUid());
        map.put("meetingName", meetingName);
        map.put("meetingDescription", meetingDescription);
        map.put("meetingDate", meetingDate);
        map.put("imageUrl", imageUrl);
        map.put("timestamp", FieldValue.serverTimestamp());

        collectionReference.document(id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Wpis dodany", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    ((MainActivity) getActivity()).switchFragment(new MeetingFragment());
                });

    }
}
