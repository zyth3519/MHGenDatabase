package com.ghstudios.android.features.items.detail;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.ghstudios.android.data.DataManager;
import com.ghstudios.android.GenericActivity;
import com.ghstudios.android.MenuSection;
import com.ghstudios.android.mhgendatabase.R;

public class MaterialDetailActivity extends GenericActivity {
    public static final String EXTRA_MATERIAL_ITEM_ID = "MATERIAL_ID";
    long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getLongExtra(EXTRA_MATERIAL_ITEM_ID,0);
        setTitle(DataManager.get().getItem(id).getName());
    }

    @Override
    protected Fragment createFragment() {
        return MaterialDetailItemFragment.newInstance(getIntent().getLongExtra(EXTRA_MATERIAL_ITEM_ID,0));
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
            boolean favorited = DataManager.toggleFavorite(id);
            int msg = favorited ? R.string.favorite_added : R.string.favorite_removed;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
