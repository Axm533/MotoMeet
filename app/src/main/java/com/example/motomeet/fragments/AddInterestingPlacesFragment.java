package com.example.motomeet.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddInterestingPlacesFragment extends Fragment implements OnMapReadyCallback {

    private EditText placeNameET, placeDescriptionET;
    private AppCompatButton addEntryBtn;
    private ImageButton returnBtn;
    private FirebaseUser user;
    private GoogleMap mMap;
    private Double latitude, longitude;

    private float currentZoom;
    String apiKey = BuildConfig.API_KEY;

    public AddInterestingPlacesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_interesting_places, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapInterestingPlaces);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        init(view);
        addEntryBtn.setOnClickListener(v -> uploadEntry());

        returnBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new InterestingPlacesFragment()));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnCameraMoveListener(() -> currentZoom = mMap.getCameraPosition().zoom);


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
        placeNameET = view.findViewById(R.id.placeNameET);
        placeDescriptionET = view.findViewById(R.id.placeDescriptionET);
    }

    public void uploadEntry(){

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Interesting places");

        String placeName = placeNameET.getText().toString();
        String placeDescription = placeDescriptionET.getText().toString();

        String id = collectionReference.document().getId();

        String imageUrl = "https://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude
                + "&zoom="+currentZoom+"&size=200x200&sensor=false&markers=" + latitude + "," + longitude + "&key="+apiKey;

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("uid", user.getUid());
        map.put("placeName", placeName);
        map.put("placeDescription", placeDescription);
        map.put("imageUrl", imageUrl);
        map.put("latitude", latitude);
        map.put("longitude", longitude);

        collectionReference.document(id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Wpis dodany", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    ((MainActivity) getActivity()).switchFragment(new InterestingPlacesFragment());
                });

    }
}