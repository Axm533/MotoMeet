package com.example.motomeet.fragments;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.motomeet.R;
import com.example.motomeet.adapter.GalleryAdapter;
import com.example.motomeet.model.GalleryImages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFragment extends Fragment {
    Uri imageUri;
    Dialog dialog;
    private EditText descET;
    private ImageView imageViewAddPost;
    private RecyclerView recyclerView;
    private AppCompatButton addEntryBtn;
    private List<GalleryImages> list;
    private GalleryAdapter adapter;
    private FirebaseUser user;

    private ActivityResultLauncher<Intent> cropImageActivityLauncher;
    public AddFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);

        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);

        recyclerView.setAdapter(adapter);

        clickListener();
    }

    private void init(View view) {

        descET = view.findViewById(R.id.descriptionET);
        imageViewAddPost = view.findViewById(R.id.imageViewAddPost);
        recyclerView = view.findViewById(R.id.recyclerView);
        addEntryBtn = view.findViewById(R.id.addEntryBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_background, null));
        dialog.setCancelable(false);

        cropImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                        imageUri = cropResult.getUri();
                        Glide.with(getContext()).load(imageUri).into(imageViewAddPost);
                        imageViewAddPost.setVisibility(View.VISIBLE);
                        addEntryBtn.setVisibility(View.VISIBLE);
                    }
                }
        );
    }
    private void clickListener() {

        adapter.SendImage(picUri -> {
            Intent intent = CropImage.activity(picUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4, 3)
                    .getIntent(getContext());
            cropImageActivityLauncher.launch(intent);
        });


        addEntryBtn.setOnClickListener(v -> {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference().child("Post Images/" + System.currentTimeMillis());

            dialog.show();

            storageReference.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> uploadData(uri.toString()));
                        } else {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Failed to upload post", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
    private void uploadData(String imageURL) {

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Post Images");

        String id = collectionReference.document().getId();

        String description = descET.getText().toString();

        List<String> likes = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("description", description);
        map.put("imageUrl", imageURL);
        map.put("timestamp", FieldValue.serverTimestamp());
        map.put("name", user.getDisplayName());
        map.put("profileImage", String.valueOf(user.getPhotoUrl()));
        map.put("likes", likes);
        map.put("uid", user.getUid());

        collectionReference.document(id).set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        super.onResume();

        getActivity().runOnUiThread(() -> Dexter.withContext(getContext())
                .withPermissions(READ_MEDIA_IMAGES)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {

                            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download");

                            if (file.exists()) {
                                File[] files = file.listFiles();
                                assert files != null;

                                list.clear();

                                for (File file1 : files) {

                                    if (file1.getAbsolutePath().endsWith(".jpg") || file1.getAbsolutePath().endsWith(".png")) {

                                        list.add(new GalleryImages(Uri.fromFile(file1)));
                                        adapter.notifyDataSetChanged();

                                    }
                                }
                            }
                        }else{
                            Toast.makeText(getContext(), "Please grant permission", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(dexterError -> Toast.makeText(getContext(), "Error"+dexterError.toString(), Toast.LENGTH_SHORT).show()).check());
    }

}