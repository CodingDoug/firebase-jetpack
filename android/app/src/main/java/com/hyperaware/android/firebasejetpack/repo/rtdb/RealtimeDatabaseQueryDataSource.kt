/*
 * Copyright 2018 Google LLC
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

package com.hyperaware.android.firebasejetpack.repo.rtdb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.hyperaware.android.firebasejetpack.repo.paging.LoadingState

/**
 * This only works for RTDB queries whose ordered query keys are fully unique
 * (for example, orderByKey() queries.  This is because the next child that
 * comes "after" a given child must be ordered strictly greater that the given
 * child.  If two children have the same key, and they are split on page
 * boundaries during paging, they would be impossible to uniquely identify,
 * which messes up the paging.
 */

class RealtimeDatabaseQueryDataSource private constructor(private val baseQuery: Query)
    : PageKeyedDataSource<PageKey, DataSnapshot>() {


    companion object {
        private const val TAG = "RtdbQueryDataSource"
    }

    class Factory(private val query: Query) : DataSource.Factory<PageKey, DataSnapshot>() {
        override fun create(): DataSource<PageKey, DataSnapshot> {
            return RealtimeDatabaseQueryDataSource(query)
        }
    }

    private val mutableLoadingState = MutableLiveData<LoadingState>()

    val loadingState: LiveData<LoadingState>
        get() = mutableLoadingState

    override fun loadInitial(params: LoadInitialParams<PageKey>, callback: LoadInitialCallback<PageKey, DataSnapshot>) {
        // Set initial loading state
        mutableLoadingState.postValue(LoadingState.LOADING_INITIAL)

        baseQuery
            .limitToFirst(params.requestedLoadSize)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mutableLoadingState.postValue(LoadingState.LOADED)
                    val results = snapshot.children.toList()
                    val nextPageKey = getNextPageKey(results)
                    callback.onResult(results, null, nextPageKey)
                }
                override fun onCancelled(error: DatabaseError) {
                    mutableLoadingState.postValue(LoadingState.ERROR)
                    Log.e(TAG, "Error loading paged items", error.toException())
                }
            })
    }

    override fun loadAfter(params: LoadParams<PageKey>, callback: LoadCallback<PageKey, DataSnapshot>) {
        mutableLoadingState.postValue(LoadingState.LOADING_MORE)

        // Query for items starting at the given key, but exclude the first
        // item from the results, as we already have it.
        baseQuery
            .startAt(params.key.startAtKey)
            .limitToFirst(params.requestedLoadSize + 1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mutableLoadingState.postValue(LoadingState.LOADED)
                    val children = snapshot.children.toList()
                    val results = if (children.size > 1) {
                        children.takeLast(children.size - 1)
                    }
                    else {
                        emptyList<DataSnapshot>()
                    }
                    val nextPageKey = getNextPageKey(results)
                    callback.onResult(results, nextPageKey)
                    if (results.isEmpty()) {
                        mutableLoadingState.postValue(LoadingState.FINISHED)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    mutableLoadingState.postValue(LoadingState.ERROR)
                    Log.e(TAG, "Error loading paged items", error.toException())
                }
            })
    }

    override fun loadBefore(params: LoadParams<PageKey>, callback: LoadCallback<PageKey, DataSnapshot>) {
        // The paging here only understands how to append new items to the
        // results, not prepend items from earlier pages.
        callback.onResult(emptyList(), null)
    }

    private fun getNextPageKey(page: List<DataSnapshot>): PageKey? {
        return if (page.isEmpty()) {
            null
        }
        else {
            PageKey(page.last().key!!)
        }
    }

}


/**
 * The PageKey for RealtimeDatabase query results is the key of the last item
 * in the page.  The next page will use this to request this item in addition
 * to the next page of results using startAt().  That first item needs to be
 * removed from the page before it's sent to the caller.
 */

data class PageKey(val startAtKey: String)
