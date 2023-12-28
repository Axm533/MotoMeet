package com.example.motomeet.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.motomeet.R;
import com.example.motomeet.ReplacerActivity;
import com.example.motomeet.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    private final List<HomeModel> list;
    Activity context;
    OnPressed onPressed;

    public HomeAdapter(List<HomeModel> list, Activity context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new HomeHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        holder.usernameTv.setText(list.get(position).getName());
        holder.timeTv.setText(""+list.get(position).getTimestamp());

        List<String> likes = list.get(position).getLikes();

        int likesCount = likes.size();

        if(likesCount == 0){
            holder.likeCountTv.setText("0 Likes");
        } else if (likesCount == 1) {
            holder.likeCountTv.setText(likesCount + " like");
        }else{
            holder.likeCountTv.setText(likesCount + " likes");
        }

        //assert user != null;
        if(user != null)
        {
            holder.likeCheckBox.setChecked(likes.contains(user.getUid()));
        }else{
            holder.likeCheckBox.setChecked(false);
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

        holder.clickListener(position,
                list.get(position).getId(),
                list.get(position).getName(),
                list.get(position).getUid(),
                list.get(position).getLikes(),
                list.get(position).getImageUrl());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnPressed(OnPressed onPressed){
        this.onPressed = onPressed;
    }

    public interface OnPressed {
        void onLiked(int position, String id, String uid, List<String> likes, boolean isChecked);
        void setCommentCount(TextView textView);
    }

    class HomeHolder extends RecyclerView.ViewHolder{
        private final CircleImageView profileImage;
        private final TextView usernameTv, timeTv, likeCountTv, descriptionTv;
        private final ImageView imageView;
        private final ImageButton commentBtn, shareBtn;
        private final CheckBox likeCheckBox;

        public HomeHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profImage);
            imageView = itemView.findViewById(R.id.imageView);
            usernameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likeCountTv = itemView.findViewById(R.id.likeCountTv);
            likeCheckBox = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTv = itemView.findViewById(R.id.descTv);

            TextView commentTV = itemView.findViewById(R.id.commentTV);

            onPressed.setCommentCount(commentTV);
        }

        public void clickListener(final int position, final String id, String name, final String uid, final List<String> likes, final String imageUrl){

            likeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> onPressed.onLiked(position, id, uid ,likes, isChecked));

            commentBtn.setOnClickListener(v -> {

                Log.d("LOG commfrag", "UID: "+uid + "\n\n" + "id: "+id);

                Intent intent = new Intent(context, ReplacerActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("uid", uid);
                intent.putExtra("isComment", true);

                context.startActivity(intent);

            });

        }
    }
}
