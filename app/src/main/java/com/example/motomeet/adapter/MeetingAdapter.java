package com.example.motomeet.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.motomeet.R;
import com.example.motomeet.model.MeetingModel;

import java.util.List;
import java.util.Random;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingHolder>{

    private final List<MeetingModel> list;

    Activity context;

    public MeetingAdapter(List<MeetingModel> list, Activity context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MeetingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_item, parent, false);
        return new MeetingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingHolder holder, int position) {

        holder.meetingNameTV.setText(list.get(position).getMeetingName());
        holder.meetingDescriptionTV.setText(list.get(position).getMeetingDescription());
        holder.meetingDateTV.setText(list.get(position).getMeetingDate());

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

    static class MeetingHolder extends RecyclerView.ViewHolder{

        private final TextView meetingNameTV, meetingDescriptionTV, meetingDateTV;

        private final ImageView imageView;

        public MeetingHolder(@NonNull View itemView){
            super(itemView);

            meetingNameTV = itemView.findViewById(R.id.meetingNameTV);
            meetingDescriptionTV = itemView.findViewById(R.id.meetingDescriptionTV);
            meetingDateTV = itemView.findViewById(R.id.meetingDateTV);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
