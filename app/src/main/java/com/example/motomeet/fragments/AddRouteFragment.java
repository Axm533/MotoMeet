package com.example.motomeet.fragments;

import android.app.Activity;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddRouteFragment extends Fragment implements OnMapReadyCallback {
    private String encodedPolyline;
    private Double distanceInKilometers;
    private GoogleMap mMap;
    private EditText routeNameET, routeDescriptionET;
    private Marker startMarker, endMarker;
    private AppCompatButton addRouteBtn, clearMapBtn;
    private ImageButton returnBtn;
    private FirebaseUser user;
    Activity activity;

    String apiKey = BuildConfig.API_KEY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        init(view);
        clickListener();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMapClickListener(latLng -> {
            if (startMarker == null) {
                startMarker = mMap.addMarker(new MarkerOptions().position(latLng));
            } else if (endMarker == null) {
                endMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                fetchDirections(startMarker.getPosition(), endMarker.getPosition());
            }
        });
    }

    private void init(View view){
        activity = getActivity();
        user = FirebaseAuth.getInstance().getCurrentUser();

        routeNameET = view.findViewById(R.id.routeNameET);
        routeDescriptionET = view.findViewById(R.id.routeDescriptionET);
        addRouteBtn = view.findViewById(R.id.addRouteBtn);
        clearMapBtn = view.findViewById(R.id.clearMapBtn);
        returnBtn = view.findViewById(R.id.returnBtn);
    }

    private void clickListener(){
        addRouteBtn.setOnClickListener(v -> uploadEntry());

        clearMapBtn.setOnClickListener(v -> {
            mMap.clear();
            startMarker = null;
            endMarker = null;
        });

        returnBtn.setOnClickListener(v -> ((MainActivity) getActivity()).switchFragment(new RouteFragment()));
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest){
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + apiKey;
    }

    private void fetchDirections(LatLng start, LatLng end) {
        String url = getDirectionsUrl(start, end);

        new Thread(() -> {
            try {
                URL directionUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) directionUrl.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                parseJsonData(response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void parseJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routesArray = jsonObject.getJSONArray("routes");

            if (routesArray.length() > 0) {
                JSONObject route = routesArray.getJSONObject(0);
                JSONArray legs = route.getJSONArray("legs");
                long distanceInMeters = 0;

                for (int i = 0; i < legs.length(); i++) {
                    JSONObject leg = legs.getJSONObject(i);
                    JSONObject distance = leg.getJSONObject("distance");
                    distanceInMeters += distance.getLong("value");
                }

                distanceInKilometers = distanceInMeters / 1000.0;
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                encodedPolyline = overviewPolyline.getString("points");
                List<LatLng> list = decodePoly(encodedPolyline);

                activity.runOnUiThread(() -> drawPolyline(list));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawPolyline(List<LatLng> list) {
        PolylineOptions options = new PolylineOptions()
                .width(12)
                .color(Color.RED)
                .geodesic(true);
        options.addAll(list);
        mMap.addPolyline(options);
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public void uploadEntry(){

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Routes");

        String routeName = routeNameET.getText().toString();
        String routeDescription = routeDescriptionET.getText().toString();
        String routeLength = String.format(Locale.getDefault(), "%.2f km", distanceInKilometers);

        String id = collectionReference.document().getId();

        String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?"
                + "size=200x200"
                + "&maptype=roadmap"
                + "&path=enc:" + encodedPolyline
                + "&key="+apiKey;

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("uid", user.getUid());
        map.put("routeName", routeName);
        map.put("routeDescription", routeDescription);
        map.put("routeLength", routeLength);
        map.put("startLat", startMarker.getPosition().latitude);
        map.put("startLong", startMarker.getPosition().longitude);
        map.put("endLat", endMarker.getPosition().latitude);
        map.put("endLong", endMarker.getPosition().longitude);
        map.put("imageUrl", imageUrl);
        map.put("timestamp", FieldValue.serverTimestamp());

        collectionReference.document(id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Wpis dodany", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    ((MainActivity) getActivity()).switchFragment(new RouteFragment());
                });

    }
}
