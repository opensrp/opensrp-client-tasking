package org.smartregister.tasking.adapter;


import android.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 30-07-2020.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList();
    private final List<String> mFragmentTitleList = new ArrayList();
    private Pair<Integer, Integer> mPositionCountPair;

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public Fragment getItem(int position) {
        return (Fragment)this.mFragmentList.get(position);
    }

    public int getCount() {
        return this.mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        this.mFragmentList.add(fragment);
        this.mFragmentTitleList.add(title);
    }

    public CharSequence getPageTitle(int position) {
        String title = (String)this.mFragmentTitleList.get(position);
        if (this.mPositionCountPair != null && (Integer)this.mPositionCountPair.first == position) {
            title = title + " (" + this.mPositionCountPair.second + ")";
        }

        return title;
    }

    public void updateCount(Pair<Integer, Integer> positionCountPair) {
        if (positionCountPair.first != null && positionCountPair.second != null) {
            this.mPositionCountPair = positionCountPair;
            this.notifyDataSetChanged();
        }

    }
}