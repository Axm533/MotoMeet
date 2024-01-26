package com.example.motomeet.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.motomeet.R;
import com.example.motomeet.model.RouteModel;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteHolder> {

    private final List<RouteModel> list;
    Activity context;
    Location currentUserLocation;

    public RouteAdapter(List<RouteModel> list, Activity context, Location location) {
        this.list = list;
        this.context = context;
        this.currentUserLocation = location;
    }

    @NonNull
    @Override
    public RouteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_items, parent, false);
        return new RouteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteHolder holder, int position) {

        holder.routeNameTV.setText(list.get(position).getRouteName());
        holder.routeDescriptionTV.setText(list.get(position).getRouteDescription());
        holder.routeLengthTV.setText(list.get(position).getRouteLength());
        double distanceStart = calculateDistance(currentUserLocation.getLatitude(),currentUserLocation.getLongitude(), list.get(position).getStartLat(), list.get(position).getStartLong());
        double distanceEnd = calculateDistance(currentUserLocation.getLatitude(),currentUserLocation.getLongitude(), list.get(position).getEndLat(), list.get(position).getEndLong());
        double displayDistance = Math.min(distanceStart, distanceEnd);

        String distanceText = String.format(Locale.getDefault(), "%.2f km", displayDistance);
        holder.routeDistanceToUserTV.setText(distanceText);

        Random random = new Random();

        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getImageUrl())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class RouteHolder extends RecyclerView.ViewHolder{
        private final TextView routeNameTV, routeDescriptionTV, routeLengthTV, routeDistanceToUserTV;

        private final ImageView imageView;
        public RouteHolder(@NonNull View itemView){
            super(itemView);

            routeNameTV = itemView.findViewById(R.id.routeNameTV);
            routeDescriptionTV = itemView.findViewById(R.id.routeDescriptionTV);
            routeLengthTV = itemView.findViewById(R.id.routeLengthTV);
            imageView = itemView.findViewById(R.id.imageView);
            routeDistanceToUserTV = itemView.findViewById(R.id.routeDistanceToUserTV);
        }

    }

    private double calculateDistance(double startLat, double startLng, double endLat, double endLng) {
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        return results[0] / 1000.0;
    }

}
