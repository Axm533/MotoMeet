package com.example.motomeet.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.motomeet.R;
import com.example.motomeet.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    OnUserClicked onUserClicked;
    List<UserModel> list;

    public UserAdapter(List<UserModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_items, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, final int position) {

        if (list.get(position).getUid().equals(user.getUid())) {
            holder.relativeLayout.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else {
            holder.relativeLayout.setVisibility(View.VISIBLE);
        }

        holder.nameTV.setText(list.get(position).getName());
        holder.statusTV.setText(list.get(position).getStatus());

        Glide.with(holder.itemView.getContext().getApplicationContext())
                .load(list.get(position).getProfileImage())
                .placeholder(R.drawable.baseline_person_24)
                .timeout(6500)
                .into(holder.profileImage);

        holder.itemView.setOnClickListener(v -> onUserClicked.onClicked(list.get(position).getUid()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnUserClicked(OnUserClicked onUserClicked) {
        this.onUserClicked = onUserClicked;
    }

    public interface OnUserClicked {
        void onClicked(String uid);
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView nameTV, statusTV;
        RelativeLayout relativeLayout;
        public UserHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            nameTV = itemView.findViewById(R.id.nameTV);
            statusTV = itemView.findViewById(R.id.statusTV);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
        }
    }

}
