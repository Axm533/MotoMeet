package com.example.motomeet.fragments;

import static android.app.Activity.RESULT_OK;
import static com.example.motomeet.MainActivity.IS_SEARCHED_USER;
import static com.example.motomeet.MainActivity.USER_ID;
import static com.example.motomeet.utils.Constants.PREF_DIRECTORY;
import static com.example.motomeet.utils.Constants.PREF_NAME;
import static com.example.motomeet.utils.Constants.PREF_STORED;
import static com.example.motomeet.utils.Constants.PREF_URL;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.example.motomeet.ReplacerActivity;
import com.example.motomeet.SplashActivity;
import com.example.motomeet.model.PostImageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private TextView nameTv, toolbarTv, statusTv, followingCountTv, followersCountTv, postCountTv;
    private CircleImageView profileImage;
    private Button followBtn;
    private ImageButton editProfPic, menuBtn;
    private RecyclerView recyclerView;
    private LinearLayout countLayout;
    private FirebaseUser user;
    private FirebaseAuth auth;
    DocumentReference userRef, myRef;
    boolean isMyProfile = true;
    boolean isFollowed;
    List<String> followersList, followingList, followingList_2;
    String userUID;
    int postCount;
    FirestoreRecyclerAdapter<PostImageModel, PostImageHolder> adapter;
    public ProfileFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        }else{
            followBtn.setVisibility(View.VISIBLE);
            //countLayout.setVisibility(View.GONE);
            editProfPic.setVisibility(View.GONE);
        }

        userRef = FirebaseFirestore.getInstance().collection("Users").document(userUID);
        
        loadBasicData();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        loadPostImages();

        recyclerView.setAdapter(adapter);

        clickListener();
    }

    private void init(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
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
        menuBtn = view.findViewById(R.id.menuBtn);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void clickListener()
    {
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(auth.getCurrentUser() != null)
                    auth.signOut();

                startActivity(new Intent(getActivity().getApplicationContext(), ReplacerActivity.class));

            }
        });

        editProfPic.setOnClickListener(v -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(getContext(), ProfileFragment.this));

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
                        followBtn.setText("Follow");

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

                createNotification();

                followersList.add(user.getUid()); //opposite user
                followingList_2.add(userUID); //us

                final Map<String, Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);

                Map<String, Object> map = new HashMap<>();
                map.put("followers", followersList);

                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText("UnFollow");

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
                Log.e("Tag_0", error.getMessage());
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

                    //assert getContext() != null;

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
                    followBtn.setText("Unfollow");
                    isFollowed = true;
                    //startChatBtn.setVisibility(View.VISIBLE);

                } else {
                    isFollowed = false;
                    followBtn.setText("Follow");
                    //startChatBtn.setVisibility(View.GONE);
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

        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_items, parent, false);
                return new PostImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostImageHolder holder, int position, @NonNull PostImageModel model) {

                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
                postCount = getItemCount();
                postCountTv.setText("" + postCount);

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
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (result == null)
                return;
            uploadImage(result.getUri());
        }
    }

    private void uploadImage(Uri uri) {

        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        reference.putFile(uri)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        reference.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> {
                                    String imageURL = uri1.toString();

                                    UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                                    request.setPhotoUri(uri1);

                                    user.updateProfile(request.build());

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("profileImage", imageURL);

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(user.getUid())
                                            .update(map).addOnCompleteListener(task1 -> {

                                                if (task1.isSuccessful())
                                                    Toast.makeText(getContext(),
                                                            "Updated Successful", Toast.LENGTH_SHORT).show();
                                                else {
                                                    assert task1.getException() != null;
                                                    Toast.makeText(getContext(),
                                                            "Error: " + task1.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                });

                    } else {
                        assert task.getException() != null;
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                });

    }

    void createNotification() {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Notifications");

        String id = reference.document().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("time", FieldValue.serverTimestamp());
        map.put("notification", user.getDisplayName() + " followed you.");
        map.put("id", id);
        map.put("uid", userUID);

        reference.document(id).set(map);
    }

    private static class PostImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public PostImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

        }
    }

}