package com.example.motomeet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.motomeet.R;
import com.example.motomeet.model.HomeModel;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {

    private List<HomeModel> list;
    Context context;

    public HomeAdapter(List<HomeModel> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {

        holder.usernameTv.setText(list.get(position).getName());
        holder.timeTv.setText(""+list.get(position).getTimestamp());

        List<String> likesList = list.get(position).getLikes();
        int likesCount = likesList.size();

        if(likesCount == 0){
            holder.likeCountTv.setVisibility(View.INVISIBLE);
        } else if (likesCount == 1) {
            holder.likeCountTv.setText(likesCount + " like");
        }else{
            holder.likeCountTv.setText(likesCount + " likes");
        }

        holder.descriptionTv.setText(list.get(position).getDescription());

        Random random = new Random();

        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Glide.with(context.getApplicationContext())
            .load(list.get(position).getProfileImage())
            .placeholder(R.drawable.baseline_person_24)
            .timeout(6500)
            .into(holder.profileImage);

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

    static class HomeHolder extends RecyclerView.ViewHolder{
        private final CircleImageView profileImage;
        private final TextView usernameTv, timeTv, likeCountTv, descriptionTv;
        private final ImageView imageView;
        private final ImageButton likeBtn, commentBtn, shareBtn;

        public HomeHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profImage);
            imageView = itemView.findViewById(R.id.imageView);
            usernameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likeCountTv = itemView.findViewById(R.id.likeCountTv);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTv = itemView.findViewById(R.id.descTv);
        }
    }
}
