package com.example.motomeet.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.motomeet.fragments.AddFragment;
import com.example.motomeet.fragments.HomeFragment;
import com.example.motomeet.fragments.ProfileFragment;
import com.example.motomeet.fragments.SearchFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    int noOfTabs;
    public ViewPagerAdapter(@NonNull FragmentManager fm, int noOfTabs){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.noOfTabs = noOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position){

        switch (position){

            default:
                case 0:
                    return new HomeFragment();

            case 1:
                return new SearchFragment();

            case 2:
                return new AddFragment();

            case 3:
                return new ProfileFragment();

        }
    }

    @Override
    public int getCount(){
        return noOfTabs;
    }
}
