package com.example.motomeet.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.example.motomeet.adapter.InterestingPlaceAdapter;
import com.example.motomeet.adapter.RouteAdapter;
import com.example.motomeet.model.RouteModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteFragment extends Fragment {

    private List<RouteModel> list;

    private ImageButton addEntryBtn, returnBtn;

    private RecyclerView recyclerView;

    private RouteAdapter adapter;

    Location currentLocation;

    private FusedLocationProviderClient fusedLocationProviderClient;

    public RouteFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getCurrentLocation();

        addEntryBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new AddRouteFragment()));

        returnBtn.setOnClickListener(v -> startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class)));
    }

    private void init(View view){
        addEntryBtn = view.findViewById(R.id.addEntryBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
        list = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void loadEntries(){
        list.clear();

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Routes");

        collectionReference.addSnapshotListener((value, error) -> {
                    if(error != null) return;

                    if(value == null) return;

                    List<RouteModel> tempRoutes = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : value) {
                        RouteModel model = snapshot.toObject(RouteModel.class);
                        tempRoutes.add(model);
                    }

                    // Sortowanie tras
                    Collections.sort(tempRoutes, (route1, route2) -> {
                        double distanceStart1 = calculateDistance(currentLocation.getLatitude(),currentLocation.getLongitude(), route1.getStartLat(), route1.getStartLong());
                        double distanceEnd1 = calculateDistance(currentLocation.getLatitude(),currentLocation.getLongitude(), route1.getEndLat(), route1.getEndLong());
                        double distance1 = Math.min(distanceStart1, distanceEnd1);

                        double distanceStart2 = calculateDistance(currentLocation.getLatitude(),currentLocation.getLongitude(), route2.getStartLat(), route2.getStartLong());
                        double distanceEnd2 = calculateDistance(currentLocation.getLatitude(),currentLocation.getLongitude(), route2.getEndLat(), route2.getEndLong());
                        double distance2 = Math.min(distanceStart2, distanceEnd2);

                        return Double.compare(distance1, distance2);
                    });

                    list.clear();
                    list.addAll(tempRoutes);
                    adapter.notifyDataSetChanged();
                });
    }
    private double calculateDistance(double startLat, double startLng, double endLat, double endLng) {
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        return results[0] / 1000.0;
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){

        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {

                Location location = task.getResult();

                if (location != null) {
                    currentLocation = location;
                    adapter = new RouteAdapter(list, getActivity(), currentLocation);
                    recyclerView.setAdapter(adapter);
                    loadEntries();
                } else {
                    LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                            .setIntervalMillis(10000)
                            .setMinUpdateIntervalMillis(1000)
                            .setMaxUpdates(1)
                            .build();

                    LocationCallback locationCallback = new LocationCallback() {
                        @Override
                        public void
                        onLocationResult(LocationResult locationResult) {
                            currentLocation = locationResult.getLastLocation();
                            adapter = new RouteAdapter(list, getActivity(), currentLocation);
                            recyclerView.setAdapter(adapter);
                            loadEntries();
                        }
                    };
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            });
        } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && (grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            Toast.makeText(getContext(), "Brak pozwolenia na lokalizację", Toast.LENGTH_SHORT).show();
        }
    }
}