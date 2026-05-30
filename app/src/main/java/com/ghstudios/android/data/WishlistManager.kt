package com.ghstudios.android.data

import android.content.Context
import com.ghstudios.android.data.classes.Wishlist
import com.ghstudios.android.data.classes.WishlistData
import com.ghstudios.android.data.database.MonsterHunterDatabaseHelper
import com.ghstudios.android.util.firstOrNull
import com.ghstudios.android.util.toList

private const val FAVORITES_LIST_NAME = "Favorites"

class WishlistManager internal constructor(
    private val mAppContext: Context,
    private val mBaseManager: DataManager,
    private val mHelper: MonsterHunterDatabaseHelper
) {
    val TAG = "WishlistManager"

    private fun getOrCreateFavoritesList(): Long {
        val lists = mHelper.queryWishlists().toList { it.wishlist }
        val existing = lists.firstOrNull()
        return existing?.id ?: mHelper.queryAddWishlist(FAVORITES_LIST_NAME)
    }

    fun getFavorites(): List<WishlistData> {
        val listId = getOrCreateFavoritesList()
        return mHelper.queryWishlistData(listId).toList { it.wishlistData }
    }

    fun isFavorited(itemId: Long): Boolean {
        val listId = getOrCreateFavoritesList()
        val entry = mHelper.queryWishlistData(listId, itemId, "")
            .firstOrNull { it.wishlistData }
        return entry != null
    }

    fun toggleFavorite(itemId: Long): Boolean {
        val listId = getOrCreateFavoritesList()
        val entry = mHelper.queryWishlistData(listId, itemId, "")
            .firstOrNull { it.wishlistData }
        return if (entry != null) {
            mHelper.queryDeleteWishlistData(entry.id); false
        } else {
            mHelper.queryAddWishlistData(listId, itemId, 1, ""); true
        }
    }

    fun removeFavorite(dataId: Long) {
        mHelper.queryDeleteWishlistData(dataId)
    }

    @Deprecated("Use getFavorites()", ReplaceWith("getFavorites()"))
    fun getWishlists(): List<Wishlist> {
        return mHelper.queryWishlists().toList { it.wishlist }
    }
}
