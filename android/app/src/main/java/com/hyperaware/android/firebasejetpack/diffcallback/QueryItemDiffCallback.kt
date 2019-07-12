/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyperaware.android.firebasejetpack.diffcallback

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
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

    @SuppressLint("DiffUtilEquals")  // equals() is OK for data classes
    override fun areContentsTheSame(oldItem: QueryItem<T>, newItem: QueryItem<T>): Boolean {
        return oldItem.item == newItem.item
    }
}
