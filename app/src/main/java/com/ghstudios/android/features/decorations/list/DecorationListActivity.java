package com.ghstudios.android.features.decorations.list;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.ghstudios.android.mhgendatabase.R;
import com.ghstudios.android.GenericActivity;
import com.ghstudios.android.MenuSection;

public class DecorationListActivity extends GenericActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_decorations);

        // Enable drawer button instead of back button
        super.enableDrawerIndicator();

        // Tag as top level activity
        super.setAsTopLevel();
    }

    @Override
    protected int getSelectedSection() {
        return MenuSection.DECORATION;
    }

    @Override
    protected Fragment createFragment() {
        return new DecorationListFragment();
    }
}
