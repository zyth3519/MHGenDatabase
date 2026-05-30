package com.ghstudios.android.features.wishlist.list

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

import com.ghstudios.android.AssetLoader
import com.ghstudios.android.ClickListeners.ItemClickListener
import com.ghstudios.android.RecyclerViewFragment
import com.ghstudios.android.adapter.common.SimpleDiffRecyclerViewAdapter
import com.ghstudios.android.adapter.common.SimpleViewHolder
import com.ghstudios.android.adapter.common.SwipeReorderTouchHelper
import com.ghstudios.android.data.classes.WishlistData
import com.ghstudios.android.mhgendatabase.R
import com.ghstudios.android.util.createSnackbarWithUndo

class FavoriteItemAdapter : SimpleDiffRecyclerViewAdapter<WishlistData>() {
    override fun areItemsTheSame(oldItem: WishlistData, newItem: WishlistData): Boolean = oldItem.id == newItem.id

    override fun onCreateView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.fragment_wishlist_item_listitem, parent, false)
    }

    override fun bindView(viewHolder: SimpleViewHolder, data: WishlistData) {
        val view = viewHolder.itemView
        val item = data.item
        val root = view.findViewById<LinearLayout>(R.id.listitem)
        val iv = view.findViewById<ImageView>(R.id.item_image)
        val tv = view.findViewById<TextView>(R.id.item)
        view.findViewById<TextView>(R.id.amt).visibility = View.GONE
        view.findViewById<TextView>(R.id.extra).visibility = View.GONE

        tv.text = item.name
        AssetLoader.setIcon(iv, item)
        view.tag = data.id
        root.setOnClickListener(ItemClickListener(viewHolder.context, item))
    }
}

class WishlistListFragment : RecyclerViewFragment() {
    val viewModel by lazy { ViewModelProvider(this).get(WishlistListViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableDivider()
        val adapter = FavoriteItemAdapter()
        setAdapter(adapter)

        val handler = ItemTouchHelper(SwipeReorderTouchHelper(afterSwiped = {
            val dataId = it.itemView.tag as Long
            val op = viewModel.startDeleteFavorite(dataId)
            view.findViewById<ViewGroup>(R.id.recyclerview_container_main)
                .createSnackbarWithUndo(getString(R.string.wishlist_deleted), op)
        }))
        handler.attachToRecyclerView(recyclerView)

        viewModel.favoriteItems.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            adapter.setItems(it)
            showEmptyView(show = it.isEmpty())
        })
    }

    override fun onResume() { super.onResume(); viewModel.reload() }
}
