package com.ghstudios.android.features.skills;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.cursoradapter.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ghstudios.android.data.classes.SkillTree;
import com.ghstudios.android.data.cursors.SkillTreeCursor;
import com.ghstudios.android.loader.SkillTreeSearchCursorLoader;
import com.ghstudios.android.mhgendatabase.R;
import com.ghstudios.android.ClickListeners.SkillClickListener;

public class SkillTreeListFragment extends ListFragment implements
        LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = R.id.skill_tree_list_fragment;
    private EditText searchInput;
    private String currentQuery = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = new Bundle();
        args.putString("searchTerm", "");
        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_skilltree_search_list, parent, false);

        searchInput = v.findViewById(R.id.skill_search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String q = s.toString();
                if (q.equals(currentQuery)) return;
                currentQuery = q;
                Bundle args = new Bundle();
                args.putString("searchTerm", q);
                getLoaderManager().restartLoader(LOADER_ID, args, SkillTreeListFragment.this);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String searchTerm = args != null ? args.getString("searchTerm", "") : "";
        return new SkillTreeSearchCursorLoader(getActivity(), searchTerm);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        SkillTreeListCursorAdapter adapter = new SkillTreeListCursorAdapter(
                getActivity(), (SkillTreeCursor) cursor);
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setListAdapter(null);
    }

    private class SkillTreeListCursorAdapter extends CursorAdapter {

        private SkillTreeCursor mSkillTreeCursor;

        public SkillTreeListCursorAdapter(Context context, SkillTreeCursor cursor) {
            super(context, cursor, 0);
            mSkillTreeCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.fragment_skilltree_listitem, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final SkillTree skilltree = mSkillTreeCursor.getSkillTree();
            LinearLayout itemLayout = (LinearLayout) view.findViewById(R.id.listitem);

            TextView skilltreeNameTextView = (TextView) view.findViewById(R.id.item);
            String cellText = skilltree.getName();
            skilltreeNameTextView.setText(cellText);

            itemLayout.setOnClickListener(new SkillClickListener(context, skilltree.getId()));
        }
    }
}
