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
import com.example.motomeet.model.InterestingPlaceModel;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class InterestingPlaceAdapter extends RecyclerView.Adapter<InterestingPlaceAdapter.InterestingPlaceHolder> {

    private final List<InterestingPlaceModel> list;

    Activity context;

    Location currentUserLocation;

    public InterestingPlaceAdapter(List<InterestingPlaceModel> list, Activity context, Location currentUserLocation) {
        this.list = list;
        this.context = context;
        this.currentUserLocation = currentUserLocation;
    }

    @NonNull
    @Override
    public InterestingPlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.interesting_place_item, parent, false);
        return new InterestingPlaceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestingPlaceHolder holder, int position) {
        holder.placeNameTV.setText(list.get(position).getPlaceName());
        holder.placeDescriptionTV.setText(list.get(position).getPlaceDescription());

        float[] results = new float[1];
        Location.distanceBetween(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                list.get(position).getLatitude(), list.get(position).getLongitude(), results);
        String distance = String.format(Locale.getDefault(),"%.2f km", results[0] / 1000);
        holder.placeDistanceTV.setText(distance);

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

    static class InterestingPlaceHolder extends RecyclerView.ViewHolder{

        private final TextView placeNameTV, placeDescriptionTV, placeDistanceTV;

        private final ImageView imageView;
        public InterestingPlaceHolder(@NonNull View viewItem){
            super(viewItem);

            placeNameTV = viewItem.findViewById(R.id.placeNameTV);
            placeDescriptionTV = viewItem.findViewById(R.id.placeDescriptionTV);
            placeDistanceTV = viewItem.findViewById(R.id.placeDistanceTV);
            imageView = itemView.findViewById(R.id.imageView);
        }

    }

}
