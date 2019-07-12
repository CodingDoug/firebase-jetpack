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
import com.hyperaware.android.firebasejetpack.repo.QueryItemOrException

/**
 * T must be a data class for this to work, as it depends on the structural
 * equality to compare objects.
 */

class QueryItemOrExceptionDiffUtilItemCallback<T> : DiffUtil.ItemCallback<QueryItemOrException<T>>() {

    override fun areItemsTheSame(oldItem: QueryItemOrException<T>, newItem: QueryItemOrException<T>): Boolean {
        return if (oldItem.data != null && newItem.data != null) {
            oldItem.data.id == newItem.data.id
        }
        else {
            oldItem === newItem
        }
    }

    @SuppressLint("DiffUtilEquals")  // equals() is OK for data classes
    override fun areContentsTheSame(oldItem: QueryItemOrException<T>, newItem: QueryItemOrException<T>): Boolean {
        return oldItem.data == newItem.data
    }

}
