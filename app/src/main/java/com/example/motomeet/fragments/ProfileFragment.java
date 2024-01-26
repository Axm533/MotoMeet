package com.example.motomeet.fragments;

import static android.app.Activity.RESULT_OK;
import static com.example.motomeet.MainActivity.IS_SEARCHED_USER;
import static com.example.motomeet.MainActivity.USER_ID;
import static com.example.motomeet.utils.Constants.PREF_DIRECTORY;
import static com.example.motomeet.utils.Constants.PREF_NAME;
import static com.example.motomeet.utils.Constants.PREF_STORED;
import static com.example.motomeet.utils.Constants.PREF_URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.motomeet.ChatActivity;
import com.example.motomeet.R;
import com.example.motomeet.model.PostImageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private TextView nameTv, toolbarTv, statusTv, followingCountTv, followersCountTv, postCountTv;
    private CircleImageView profileImage;
    private AppCompatButton followBtn, startChatBtn;
    private ImageButton editProfPic;
    private RecyclerView recyclerView;
    private LinearLayout countLayout;
    private FirebaseUser user;
    DocumentReference userRef, myRef;
    boolean isMyProfile = true;
    boolean isFollowed;
    List<String> followersList, followingList, followingList_2;
    String userUID;
    FirestoreRecyclerAdapter<PostImageModel, PostImageHolder> adapter;

    private static final int CHOOSE_PIC_CODE = 1;

    public ProfileFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        myRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());

        if (IS_SEARCHED_USER) {
            isMyProfile = false;
            userUID = USER_ID;
            loadData();
        } else {
            isMyProfile = true;
            userUID = user.getUid();
        }

        if(isMyProfile){
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
            editProfPic.setVisibility(View.VISIBLE);
            startChatBtn.setVisibility(View.GONE);
        }else{
            followBtn.setVisibility(View.VISIBLE);
            editProfPic.setVisibility(View.GONE);
        }

        userRef = FirebaseFirestore.getInstance().collection("Users").document(userUID);

        loadPostImages();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);

        loadBasicData();

        clickListener();
    }

    private void init(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        nameTv = view.findViewById(R.id.nameTV);
        statusTv = view.findViewById(R.id.statusTV);
        toolbarTv = view.findViewById(R.id.toolbarNameTV);
        followersCountTv = view.findViewById(R.id.followersCountTV);
        followingCountTv = view.findViewById(R.id.followingCountTV);
        postCountTv = view.findViewById(R.id.postCountTV);
        profileImage = view.findViewById(R.id.profileImage);
        followBtn = view.findViewById(R.id.followBtn);
        recyclerView = view.findViewById(R.id.recView);
        countLayout = view.findViewById(R.id.countLayout);
        editProfPic = view.findViewById(R.id.edit_profileImage);
        startChatBtn = view.findViewById(R.id.startChatBtn);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void clickListener() {
        startChatBtn.setOnClickListener(v -> queryChat());

        assert getContext() != null;

        editProfPic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_PIC_CODE);
        });

        followBtn.setOnClickListener(v -> {

            if (isFollowed) {

                followersList.remove(user.getUid()); //opposite user

                followingList_2.remove(userUID); //us

                final Map<String, Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);

                Map<String, Object> map = new HashMap<>();
                map.put("followers", followersList);

                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText(R.string.followBtn);

                        myRef.update(map_2).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getContext(), "UnFollowed", Toast.LENGTH_SHORT).show();
                            } else {
                                assert task1.getException() != null;
                                Log.e("Tag_3", task1.getException().getMessage());
                            }
                        });

                    } else {
                        assert task.getException() != null;
                        Log.e("Tag", "" + task.getException().getMessage());
                    }
                });

            } else {

                followersList.add(user.getUid()); //opposite user
                followingList_2.add(userUID); //us

                final Map<String, Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);

                Map<String, Object> map = new HashMap<>();
                map.put("followers", followersList);

                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText(R.string.unfollowBtn);

                        myRef.update(map_2).addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                Toast.makeText(getContext(), "Followed", Toast.LENGTH_SHORT).show();
                            } else {
                                assert task12.getException() != null;
                                Log.e("tag_3_1", task12.getException().getMessage());
                            }
                        });

                    } else {
                        assert task.getException() != null;
                        Log.e("Tag", "" + task.getException().getMessage());
                    }
                });

            }

        });
    }
    private void loadData() {

        myRef.addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.e("Tag_b", error.getMessage());
                return;
            }

            if (value == null || !value.exists()) {
                return;
            }
            followingList_2 = (List<String>) value.get("following");
        });
    }
    private void loadBasicData() {

        userRef.addSnapshotListener((value, error) -> {

            if(error != null){
                Log.e("Profile fragment/loadBasicData", error.getMessage());
                return;
            }

            assert value != null;
            if (value.exists()){

                String name = value.getString("name");
                String status = value.getString("status");
                String profileURL = value.getString("profileImage");

                nameTv.setText(name);
                toolbarTv.setText(name);
                statusTv.setText(status);

                followersList = (List<String>) value.get("followers");
                followingList = (List<String>) value.get("following");

                followersCountTv.setText("" + followersList.size());
                followingCountTv.setText("" + followingList.size());

                try {

                    Glide.with(getContext().getApplicationContext())
                            .load(profileURL)
                            .placeholder(R.drawable.baseline_person_24)
                            .circleCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {

                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    storeProfileImage(bitmap, profileURL);
                                    return false;
                                }
                            })
                            .timeout(6500)
                            .into(profileImage);

                }catch(Exception e){
                    e.printStackTrace();
                }

                if (followersList.contains(user.getUid())) {
                    followBtn.setText(R.string.unfollowBtn);
                    isFollowed = true;
                    startChatBtn.setVisibility(View.VISIBLE);

                } else {
                    isFollowed = false;
                    followBtn.setText(R.string.followBtn);
                    startChatBtn.setVisibility(View.GONE);
                }
            }
        });

    }

    private void storeProfileImage(Bitmap bitmap, String url) {

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isStored = preferences.getBoolean(PREF_STORED, false);
        String urlString = preferences.getString(PREF_URL, "");

        SharedPreferences.Editor editor = preferences.edit();

        if (isStored && urlString.equals(url))
            return;

        if (IS_SEARCHED_USER)
            return;

        ContextWrapper contextWrapper = new ContextWrapper(getActivity().getApplicationContext());

        File directory = contextWrapper.getDir("image_data", Context.MODE_PRIVATE);

        if (!directory.exists()) {
            boolean isMade = directory.mkdirs();
            Log.d("Directory", String.valueOf(isMade));
        }

        File path = new File(directory, "profile.png");

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(path);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            try {
                assert outputStream != null;
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editor.putBoolean(PREF_STORED, true);
        editor.putString(PREF_URL, url);
        editor.putString(PREF_DIRECTORY, directory.getAbsolutePath());
        editor.apply();
    }

    private void loadPostImages(){

        DocumentReference documentRef = FirebaseFirestore.getInstance().collection("Users").document(userUID);

        Query query = documentRef.collection("Post Images");

        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>().setQuery(query, PostImageModel.class).build();

        adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_items, parent, false);
                return new PostImageHolder(view);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull PostImageHolder holder, int position, @NonNull PostImageModel model) {

                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
                postCountTv.setText(""+getItemCount());
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_PIC_CODE && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri uri) {

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        storageReference.putFile(uri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        storageReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                    String imageURL = uri1.toString();

                                    UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri1);

                                    user.updateProfile(request.build());

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("profileImage", imageURL);

                                    FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).update(map).
                                            addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful())
                                                    Toast.makeText(getContext(), "Updated Successful", Toast.LENGTH_SHORT).show();
                                                else {
                                                    assert task1.getException() != null;
                                                    Toast.makeText(getContext(), "Error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                });

                    } else {
                        assert task.getException() != null;
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });

    }

    private static class PostImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public PostImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    void queryChat() {

        assert getContext() != null;
        StylishAlertDialog alertDialog = new StylishAlertDialog(getContext(), StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Ładowanie czatu...");
        alertDialog.setCancellable(false);
        alertDialog.show();

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserUid = USER_ID;

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");

        reference.whereArrayContains("uid", myUid)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean chatExists = false;
                        DocumentSnapshot existingChatDocument = null;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<String> uids = (List<String>) document.get("uid");
                            if (uids != null && uids.contains(otherUserUid)) {
                                chatExists = true;
                                existingChatDocument = document;
                                break;
                            }
                        }

                        if (chatExists) {
                            alertDialog.dismissWithAnimation();
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("uid", userUID);
                            intent.putExtra("id", existingChatDocument.getId());
                            startActivity(intent);
                        } else {
                            startChat(alertDialog);
                        }

                    } else {
                        alertDialog.dismissWithAnimation();
                    }
                });

    }

    void startChat(StylishAlertDialog alertDialog) {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");

        List<String> list = new ArrayList<>();
        list.add(0, user.getUid());
        list.add(1, userUID);

        String pushID = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("id", pushID);
        map.put("lastMessage", "");
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid", list);

        reference.document(pushID).set(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                new Handler().postDelayed(() -> {
                    alertDialog.dismissWithAnimation();
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("uid", userUID);
                    intent.putExtra("id", pushID);
                    startActivity(intent);
                }, 3000);
            } else {
                alertDialog.dismissWithAnimation();
                if(task.getException() != null) {
                    Log.e("ProfileFragment", "Error starting chat: " + task.getException().getMessage());
                }
            }
        });
    }

}