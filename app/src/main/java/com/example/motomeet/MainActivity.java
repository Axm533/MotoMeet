package com.example.motomeet;

import static com.example.motomeet.utils.Constants.PREF_DIRECTORY;
import static com.example.motomeet.utils.Constants.PREF_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TableLayout;

import com.example.motomeet.adapter.ViewPagerAdapter;
import com.example.motomeet.fragments.SearchFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SearchFragment.OnDataPass {
    public static String USER_ID;
    public static boolean IS_SEARCHED_USER = false;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        addTabs();
    }
    private void init(){
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }
    private void addTabs(){

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.outline_home_24));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.baseline_search_24));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.outline_add_box_24));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.outline_notifications_24));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.baseline_person_outline_24));

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String directory = preferences.getString(PREF_DIRECTORY, "");

        Bitmap bitmap = loadProfileImage(directory);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.baseline_home_24);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()){

                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.baseline_home_24);
                        break;

                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.baseline_search_24);
                        break;

                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.baseline_add_box_24);
                        break;

                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.baseline_notifications_24);
                        break;

                    case 4:
                        tabLayout.getTabAt(4).setIcon(R.drawable.baseline_person_24);
                        break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){

                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.outline_home_24);
                        break;

                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.baseline_search_24);
                        break;

                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.outline_add_box_24);
                        break;

                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.outline_notifications_24);
                        break;

                    case 4:
                        tabLayout.getTabAt(4).setIcon(R.drawable.baseline_person_outline_24);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){

                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.baseline_home_24);
                        break;

                    case 1:
                        tabLayout.getTabAt(1).setIcon(R.drawable.baseline_search_24);
                        break;

                    case 2:
                        tabLayout.getTabAt(2).setIcon(R.drawable.baseline_add_box_24);
                        break;

                    case 3:
                        tabLayout.getTabAt(3).setIcon(R.drawable.baseline_notifications_24);
                        break;

                    case 4:
                        tabLayout.getTabAt(4).setIcon(R.drawable.baseline_person_24);
                        break;
                }
            }
        });
    }

    @Override
    public void onChange(String uid) {
        USER_ID = uid;
        IS_SEARCHED_USER = true;
        viewPager.setCurrentItem(4);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 4){
            viewPager.setCurrentItem(0);
            IS_SEARCHED_USER = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus(true);
    }

    @Override
    protected void onPause() {
        updateStatus(false);
        super.onPause();
    }

    void updateStatus(boolean status) {

        Map<String, Object> map = new HashMap<>();
        map.put("online", status);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map);
    }

    private Bitmap loadProfileImage(String directory) {

        try {
            File file = new File(directory, "profile.png");
            return BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}