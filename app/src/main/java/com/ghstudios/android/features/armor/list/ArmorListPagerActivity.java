package com.ghstudios.android.features.armor.list;

import android.content.Intent;

import com.ghstudios.android.data.classes.Armor;
import com.ghstudios.android.mhgendatabase.R;
import com.ghstudios.android.BasePagerActivity;
import com.ghstudios.android.MenuSection;

public class ArmorListPagerActivity extends BasePagerActivity {

    @Override
    public void onAddTabs(TabAdder tabs) {
        setTitle(R.string.title_armor_sets);

        tabs.addTab(R.string.armor_type_blade_short, () ->
                ArmorExpandableListFragment.newInstance(Armor.ARMOR_TYPE_BLADEMASTER)
        );

        tabs.addTab(R.string.armor_type_gunner_short, () ->
                ArmorExpandableListFragment.newInstance(Armor.ARMOR_TYPE_GUNNER)
        );

        // Tag as top level activity
        super.setAsTopLevel();
    }

    @Override
    protected int getSelectedSection() {
        return MenuSection.ARMOR;
    }
}
