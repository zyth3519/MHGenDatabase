package com.ghstudios.android.features.wishlist.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ghstudios.android.data.DataManager
import com.ghstudios.android.data.classes.WishlistData
import com.ghstudios.android.util.UndoableOperation
import com.ghstudios.android.util.loggedThread

class WishlistListViewModel : ViewModel() {
    private val wm = DataManager.get().wishlistManager
    private var prev: UndoableOperation? = null
    val favoriteItems = MutableLiveData<List<WishlistData>>()

    init { reload() }

    fun reload() {
        prev?.complete()
        loggedThread("Reload Favorites") { favoriteItems.postValue(wm.getFavorites()) }
    }

    fun startDeleteFavorite(dataId: Long): UndoableOperation {
        val old = favoriteItems.value ?: emptyList()
        favoriteItems.value = old.filter { it.id != dataId }
        val op = UndoableOperation(
            onComplete = { wm.removeFavorite(dataId) },
            onUndo = { favoriteItems.value = old }
        )
        prev = op
        return op
    }
}
