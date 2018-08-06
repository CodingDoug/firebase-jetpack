package com.hyperaware.android.firebasejetpack.viewmodel

import android.support.v7.util.DiffUtil
import com.hyperaware.android.firebasejetpack.repo.QueryItem

/**
 * Utility for diffing lists of QueryItem elements for use with RecyclerView.
 * This code makes the assumption that the generic type T is a data class,
 * which has an automatically accurate equals() implementation.
 */

open class QueryItemDiffCallback<T> : DiffUtil.ItemCallback<QueryItem<T>>() {
    override fun areItemsTheSame(oldItem: QueryItem<T>, newItem: QueryItem<T>): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: QueryItem<T>, newItem: QueryItem<T>): Boolean {
        return oldItem.item == newItem.item
    }
}
