package com.ghstudios.android.features.decorations.list

import android.app.Activity
import androidx.lifecycle.Observer
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider

import com.ghstudios.android.ClickListeners.DecorationClickListener
import com.ghstudios.android.RecyclerViewFragment
import com.ghstudios.android.features.decorations.detail.DecorationDetailActivity


class DecorationListFragment : RecyclerViewFragment() {
    private val viewModel by lazy {
        ViewModelProvider(this).get(DecorationListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Enable the search filter and dividers
        enableDivider()
        enableFilter {
            viewModel.setFilter(it)
        }

        val maxSlots = Int.MAX_VALUE

        // Create and set the adapter
        val adapter = DecorationListAdapter(maxSlots) { decoration, view ->
            DecorationClickListener(context, decoration.id).onClick(view)
        }
        setAdapter(adapter)

        // Listen for decoration data. This updates when the filter updates as well
        viewModel.decorationData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            adapter.setItems(it)
        })
    }
}
