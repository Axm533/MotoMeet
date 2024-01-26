package com.example.motomeet.fragments;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.motomeet.ChatUsersActivity;
import com.example.motomeet.MainActivity;
import com.example.motomeet.R;
import com.example.motomeet.ReplacerActivity;
import com.example.motomeet.adapter.HomeAdapter;
import com.example.motomeet.model.HomeModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ImageButton sendBtn;

    final int navServiceBookId = R.id.nav_service_book;
    final int navCostJournalId = R.id.nav_cost_journal;
    final int navLogoutId = R.id.nav_logout;
    final int navMeetingsId = R.id.nav_meetings;
    final int navPlacesId = R.id.nav_places;
    final int navRoutesId = R.id.nav_routes;

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

        init(view);

        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();

        adapter.OnPressed((position, id, uid, likes, isChecked) -> {

            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users")
                    .document(uid)
                    .collection("Post Images")
                    .document(id);

            if (likes.contains(user.getUid())) {
                likes.remove(user.getUid());
            } else {
                likes.add(user.getUid());
            }

            Map<String, Object> map = new HashMap<>();
            map.put("likes", likes);

            documentReference.update(map);
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

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot != null) {
                List<String> uidList = (List<String>) documentSnapshot.get("following");
                if (uidList == null || uidList.isEmpty()) return;

                Query allPostsQuery = FirebaseFirestore.getInstance().collectionGroup("Post Images")
                        .whereIn("uid", uidList)
                        .orderBy("timestamp", Query.Direction.DESCENDING);

                allPostsQuery.addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.d("Error: ", e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    List<HomeModel> newPosts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HomeModel model = doc.toObject(HomeModel.class);
                        newPosts.add(model);
                    }
                    updatePostsList(newPosts);
                });
            }
        });
    }

    private void updatePostsList(List<HomeModel> newPosts) {
        list.clear();
        list.addAll(newPosts);
        adapter.notifyDataSetChanged();
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

                case navLogoutId:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Wyloguj");
                    builder.setMessage("Czy na pewno chcesz się wylogować?");
                    builder.setPositiveButton("Tak", (dialog, which) -> {
                        if(auth.getCurrentUser() != null) {
                            startActivity(new Intent(getActivity().getApplicationContext(), ReplacerActivity.class));
                            auth.signOut();
                        }
                    });
                    builder.setNegativeButton("Nie", (dialog, which) -> dialog.dismiss());
                    builder.show();

                    break;

                case navMeetingsId:
                    ((MainActivity) getActivity()).switchFragment(new MeetingFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case navPlacesId:
                    ((MainActivity) getActivity()).switchFragment(new InterestingPlacesFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case navRoutesId:
                    ((MainActivity) getActivity()).switchFragment(new RouteFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);

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