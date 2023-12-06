package com.example.motomeet.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.motomeet.R;
import com.example.motomeet.adapter.HomeAdapter;
import com.example.motomeet.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();
    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    public FirebaseUser user;
    DocumentReference documentReference;
    Activity activity;
    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();

        init(view);

        documentReference = FirebaseFirestore.getInstance().collection("Posts").document(user.getUid());
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFireStore();

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likes, boolean isChecked) {

                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if (likes.contains(user.getUid())) {
                    likes.remove(user.getUid()); // unlike
                } else {
                    likes.add(user.getUid()); // like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likes);

                reference.update(map);
            }

            @Override
            public void setCommentCount(final TextView textView) {
                commentCount.observe((LifecycleOwner) activity, integer -> {

                    assert commentCount.getValue() != null;

                    if (commentCount.getValue() == 0) {
                        textView.setVisibility(View.GONE);
                    } else
                        textView.setVisibility(View.VISIBLE);

                    StringBuilder builder = new StringBuilder();
                    builder.append("See all")
                            .append(commentCount.getValue())
                            .append(" comments");

                    textView.setText(builder);
//                    textView.setText("See all " + commentCount.getValue() + " comments");
                });
            }
        });
    }

    private void init(View view){

        Toolbar toolbar = view.findViewById(R.id.toolbar);

        if(getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void loadDataFromFireStore() {

         final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());

         final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

         documentReference.addSnapshotListener((value, error) -> {
            if(error != null){
                Log.d("Error", error.getMessage());
            }

            if(value == null)
                return;

            List<String> uidList = (List<String>) value.get("following");

             if (uidList == null || uidList.isEmpty())
                 return;

             collectionReference.whereIn("uid", uidList)
                     .addSnapshotListener((value1, error1) -> {

                         if (error1 != null) {
                             Log.d("Error: ", error1.getMessage());
                         }

                         if (value1 == null)
                             return;

                         for(QueryDocumentSnapshot snapshot : value1){

                             snapshot.getReference().collection("Post Images")
                                     .addSnapshotListener((value2, error2) -> {

                                         if (error2 != null) {
                                             Log.d("Error: ", error2.getMessage());
                                         }

                                         if (value2 == null)
                                             return;

                                         list.clear();

                                         for (final QueryDocumentSnapshot snapshot1 : value2){

                                             if(!snapshot1.exists())
                                                 return;

                                             HomeModel model = snapshot1.toObject(HomeModel.class);

                                             list.add(new HomeModel(
                                                     model.getName(),
                                                     model.getProfileImage(),
                                                     model.getImageUrl(),
                                                     model.getUid(),
                                                     model.getDescription(),
                                                     model.getId(),
                                                     model.getTimestamp(),
                                                     model.getLikes()));

                                             snapshot1.getReference().collection("Comments").get()
                                                     .addOnCompleteListener(task -> {

                                                         if (task.isSuccessful()) {

                                                             Map<String, Object> map = new HashMap<>();
                                                             for (QueryDocumentSnapshot commentSnapshot : task
                                                                     .getResult()) {
                                                                 map = commentSnapshot.getData();
                                                             }

                                                             commentCount.setValue(map.size());

                                                         }

                                                     });
                                         }
                                         adapter.notifyDataSetChanged();
                                     });
                         }
                     });
         });
    }


}