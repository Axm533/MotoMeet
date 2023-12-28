package com.example.motomeet.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motomeet.ChatUsersActivity;
import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.example.motomeet.ReplacerActivity;
import com.example.motomeet.adapter.HomeAdapter;
import com.example.motomeet.model.HomeModel;
import com.google.android.material.navigation.NavigationView;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();
    RecyclerView recyclerView;
    public DrawerLayout drawerLayout;

    public NavigationView navigationView;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;

    private FirebaseAuth auth;
    Activity activity;
    private ImageButton sendBtn;
    private Context context;

    final int navServiceBookId = R.id.nav_service_book;
    final int navCostJournalId = R.id.nav_cost_journal;
    final int navLogout = R.id.nav_logout;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();
        context = getContext();

        init(view);

        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();

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
                    builder.append("See all ")
                            .append(commentCount.getValue())
                            .append(" comments");

                    textView.setText(builder);
//                    textView.setText("See all " + commentCount.getValue() + " comments");
                });
            }
        });

        clickListener();
    }

    private void init(View view){

        Toolbar toolbar = view.findViewById(R.id.toolbar);

        if(getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        navigationView = view.findViewById(R.id.navigationView);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        sendBtn = view.findViewById(R.id.sendBtn);

    }

    private void loadDataFromFirestore() {

        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

        List<HomeModel> allPosts = new ArrayList<>();

        documentReference.addSnapshotListener((value, error) -> {

            if(error != null){
                Log.d("Error: ", error.getMessage());
                return;
            }

            if(value == null)
                return;

            List<String> uidList = (List<String>) value.get("following");

            if (uidList == null || uidList.isEmpty())
                return;

            collectionReference.whereIn("uid", uidList)
                    .addSnapshotListener((value1, error1) -> {

                        if (error1 != null) {
                            Log.d("Error1: ", error1.getMessage());
                        }

                        if (value1 == null)
                            return;

                        list.clear();

                        for(QueryDocumentSnapshot snapshot : value1){

                            snapshot.getReference().collection("Post Images")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .addSnapshotListener((value2, error2) -> {

                                        if (error2 != null) {
                                            Log.d("Error2: ", error2.getMessage());
                                        }

                                        if (value2 == null)
                                            return;

                                        for (final QueryDocumentSnapshot snapshot1 : value2){

                                            if(!snapshot1.exists())
                                                return;

                                            HomeModel model = snapshot1.toObject(HomeModel.class);

                                            allPosts.add(new HomeModel(
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
                                                            for (QueryDocumentSnapshot commentSnapshot : task.getResult()) {
                                                                map = commentSnapshot.getData();
                                                            }

                                                            commentCount.setValue(map.size());

                                                        }else{
                                                            //Toast.makeText(context, "Comments couldnt be loaded", Toast.LENGTH_SHORT).show();
                                                            Log.d("Comments didnt load: ", task.getException().getMessage());
                                                        }

                                                    });
                                        }
                                        allPosts.sort((post1, post2) -> post2.getTimestamp().compareTo(post1.getTimestamp()));
                                        list.clear();
                                        list.addAll(allPosts);
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    });
        });
    }

    private void clickListener(){

        sendBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChatUsersActivity.class)));

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            switch(id){
                case navServiceBookId:
                    ((MainActivity) getActivity()).switchFragment(new ServiceBookFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case navCostJournalId:
                    ((MainActivity) getActivity()).switchFragment(new CostJournalFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case navLogout:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Logout");
                    builder.setMessage("Are you sure you want to logout?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        if(auth.getCurrentUser() != null) {
                            startActivity(new Intent(getActivity().getApplicationContext(), ReplacerActivity.class));
                            auth.signOut();
                        }
                    });
                    builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                    builder.show();

                    break;

                default:
                    return false;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}