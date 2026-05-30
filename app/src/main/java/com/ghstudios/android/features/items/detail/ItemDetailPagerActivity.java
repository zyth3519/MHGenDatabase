package com.ghstudios.android.features.items.detail;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.ghstudios.android.BasePagerActivity;
import com.ghstudios.android.MenuSection;
import com.ghstudios.android.data.DataManager;
import com.ghstudios.android.data.classes.meta.ItemMetadata;
import com.ghstudios.android.mhgendatabase.R;

public class ItemDetailPagerActivity extends BasePagerActivity {
    public static final String EXTRA_ITEM_ID =
            "com.daviancorp.android.android.ui.detail.item_id";

    @Override
    public void onAddTabs(TabAdder tabs) {
        hideTabsIfSingular();
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        ItemDetailViewModel viewModel = new ViewModelProvider(this).get(ItemDetailViewModel.class);
        ItemMetadata meta = viewModel.setItem(itemId);
        viewModel.getItemData().observe(this, (item) -> setTitle(item.getName()));

        tabs.addTab(R.string.item_detail_tab_detail, () -> ItemDetailFragment.newInstance(itemId));
        if (meta.getUsedInCombining() || meta.getUsedInCrafting())
            tabs.addTab(R.string.item_detail_tab_usage, () -> ItemUsageFragment.newInstance(itemId));
        if (meta.isMonsterReward())
            tabs.addTab(R.string.type_monster, () -> ItemMonsterFragment.newInstance(itemId));
        if (meta.isQuestReward())
            tabs.addTab(R.string.type_quest, () -> ItemQuestFragment.newInstance(itemId));
        if (meta.isGatherable())
            tabs.addTab(R.string.type_location, ItemLocationFragment::new);
    }

    @Override
    protected int getSelectedSection() { return MenuSection.ITEMS; }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_to_wishlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_to_wishlist) {
            long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
            if (itemId != -1) {
                boolean favorited = DataManager.toggleFavorite(itemId);
                int msg = favorited ? R.string.favorite_added : R.string.favorite_removed;
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
