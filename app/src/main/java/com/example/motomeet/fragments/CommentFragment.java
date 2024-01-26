package com.example.motomeet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motomeet.R;
import com.example.motomeet.adapter.CommentAdapter;
import com.example.motomeet.model.CommentModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentFragment extends Fragment {
    EditText commentEt;
    ImageButton sendBtn;
    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    List<CommentModel> list;
    FirebaseUser user;
    String id, uid;
    CollectionReference reference;

    public CommentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        reference = FirebaseFirestore.getInstance().collection("Users").document(uid)
                .collection("Post Images")
                .document(id)
                .collection("Comments");

        loadCommentData();
        clickListener();
    }

    private void clickListener() {

        sendBtn.setOnClickListener(v -> {

            String comment = commentEt.getText().toString();

            if (comment.isEmpty()) {
                Toast.makeText(getContext(), "Enter comment", Toast.LENGTH_SHORT).show();
                return;
            }

            String commentID = reference.document().getId();

            Map<String, Object> map = new HashMap<>();
            map.put("uid", user.getUid());
            map.put("comment", comment);
            map.put("commentID", commentID);
            map.put("postID", id);
            map.put("name", user.getDisplayName());
            map.put("profileImageUrl", user.getPhotoUrl().toString());

            reference.document(commentID).set(map)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            commentEt.setText("");
                        } else {
                            assert  task.getException() != null;
                            Toast.makeText(getContext(), "Failed to comment:" + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    });

        });

    }

    private void loadCommentData() {

        reference.addSnapshotListener((value, error) -> {

            if (error != null) return;

            if (value == null) {
                Toast.makeText(getContext(), "No Comments", Toast.LENGTH_SHORT).show();
                return;
            }

            for (DocumentSnapshot snapshot : value) {
                CommentModel model = snapshot.toObject(CommentModel.class);
                list.add(model);
            }
            commentAdapter.notifyDataSetChanged();

        });

    }

    private void init(View view) {
        commentEt = view.findViewById(R.id.commentET);
        sendBtn = view.findViewById(R.id.sendBtn);
        recyclerView = view.findViewById(R.id.commentRecyclerView);

        user = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        list = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), list);
        recyclerView.setAdapter(commentAdapter);

        if (getActivity().getIntent().getExtras() == null) return;

        id = getActivity().getIntent().getExtras().getString("id");
        uid = getActivity().getIntent().getExtras().getString("uid");

    }

}