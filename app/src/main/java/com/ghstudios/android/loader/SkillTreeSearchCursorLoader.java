package com.ghstudios.android.loader;

import android.content.Context;
import android.database.Cursor;

import com.ghstudios.android.data.DataManager;

public class SkillTreeSearchCursorLoader extends SQLiteCursorLoader {

    private String searchTerm;

    public SkillTreeSearchCursorLoader(Context context, String searchTerm) {
        super(context);
        this.searchTerm = searchTerm;
    }

    @Override
    protected Cursor loadCursor() {
        return DataManager.get().querySkillTreesSearch(searchTerm);
    }
}
