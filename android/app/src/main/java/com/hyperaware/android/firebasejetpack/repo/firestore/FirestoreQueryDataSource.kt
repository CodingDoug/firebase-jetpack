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

package com.hyperaware.android.firebasejetpack.repo.firestore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.hyperaware.android.firebasejetpack.repo.paging.LoadingState

class FirestoreQueryDataSource private constructor(
    private val baseQuery: Query,
    private val source: Source
) : PageKeyedDataSource<PageKey, DocumentSnapshot>() {

    companion object {
        private const val TAG = "FirestoreQueryDataSrc"
    }

    class Factory(private val query: Query, private val source: Source)
        : DataSource.Factory<PageKey, DocumentSnapshot>() {
        override fun create(): DataSource<PageKey, DocumentSnapshot> {
            return FirestoreQueryDataSource(query, source)
        }
    }

    private val mutableLoadingState = MutableLiveData<LoadingState>()

    val loadingState: LiveData<LoadingState>
        get() = mutableLoadingState

    override fun loadInitial(
        params: LoadInitialParams<PageKey>,
        callback: LoadInitialCallback<PageKey, DocumentSnapshot>) {

        mutableLoadingState.postValue(LoadingState.LOADING_INITIAL)

        baseQuery
            .limit(params.requestedLoadSize.toLong())
            .get(source)
            .addOnSuccessListener { snapshot ->
                mutableLoadingState.postValue(LoadingState.LOADED)
                val documents = snapshot.documents
                val nextPageKey = getNextPageKey(documents)
                if (nextPageKey == null) {
                    mutableLoadingState.postValue(LoadingState.FINISHED)
                }
                callback.onResult(documents, null, nextPageKey)
            }
            .addOnFailureListener { e ->
                mutableLoadingState.postValue(LoadingState.ERROR)
                Log.e(TAG, "Error loading paged items", e)
            }
    }

    override fun loadAfter(
        params: LoadParams<PageKey>,
        callback: LoadCallback<PageKey, DocumentSnapshot>) {

        mutableLoadingState.postValue(LoadingState.LOADING_MORE)

        baseQuery
            .startAfter(params.key.startAfterDoc)
            .limit(params.requestedLoadSize.toLong())
            .get(source)
            .addOnSuccessListener { snapshot ->
                mutableLoadingState.postValue(LoadingState.LOADED)
                val documents = snapshot.documents
                val nextPageKey = getNextPageKey(documents)
                if (nextPageKey == null) {
                    mutableLoadingState.postValue(LoadingState.FINISHED)
                }
                callback.onResult(documents, nextPageKey)
            }
            .addOnFailureListener { e ->
                mutableLoadingState.postValue(LoadingState.ERROR)
                Log.e(TAG, "Error loading paged items", e)
            }
    }

    override fun loadBefore(
        params: LoadParams<PageKey>,
        callback: LoadCallback<PageKey, DocumentSnapshot>) {
        // The paging here only understands how to append new items to the
        // results, not prepend items from earlier pages.
        callback.onResult(emptyList(), null)
    }

    private fun getNextPageKey(documents: List<DocumentSnapshot>): PageKey? {
        return if (documents.isNotEmpty()) {
            PageKey(documents.last())
        }
        else {
            null
        }
    }

}


/**
 * This class holds the key to the next page as defined by the last
 * document encountered in the prior page.  This doc will be used
 * to construct the next page query using the startAfter() method
 * on the Firestore query.
 */

data class PageKey(val startAfterDoc: DocumentSnapshot)
