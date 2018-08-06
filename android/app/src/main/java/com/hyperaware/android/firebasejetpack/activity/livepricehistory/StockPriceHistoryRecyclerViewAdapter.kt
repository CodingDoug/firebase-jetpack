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

package com.hyperaware.android.firebasejetpack.activity.livepricehistory

import android.arch.lifecycle.*
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hyperaware.android.firebasejetpack.databinding.HistoricalPriceListItemBinding
import com.hyperaware.android.firebasejetpack.repo.QueryItem
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceDisplay
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceDisplayHistoryQueryResults

/**
 * This is left as an example of how NOT to work with RecyclerView using
 * LiveData.  It's inefficient because it effectively has to refresh every
 * item in the list every time there's an update anywhere in the list.
 */

internal class StockPriceHistoryRecyclerViewAdapter(
    private val liveData: LiveData<StockPriceDisplayHistoryQueryResults>,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<HistoricalPriceViewHolder>(), LifecycleObserver {

    companion object {
        private const val TAG = "StockPriceHistRVAdapter"
    }

    private var history: List<QueryItem<StockPriceDisplay>>? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun startListening() {
        liveData.observe(lifecycleOwner, historyObserver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stopListening() {
        liveData.removeObserver(historyObserver)
    }


    private val historyObserver = Observer<StockPriceDisplayHistoryQueryResults> { results ->
        if (results != null) {
            if (results.data != null) {
                history = results.data.asReversed()
            }
            else if (results.exception != null) {
                Log.e(TAG, "Observed unexpected exception", results.exception)
                TODO("Handle the error")
            }
        }

        // Note: This is inefficient because it causes the entire list
        // to refresh at every update.  It's better ot use a ListAdapter
        // since it diffs the items in the list to look for changes.
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricalPriceViewHolder {
        // Using data binding on the individual views
        val inflater = LayoutInflater.from(parent.context)
        val binding = HistoricalPriceListItemBinding.inflate(inflater, parent, false)
        return HistoricalPriceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoricalPriceViewHolder, position: Int) {
        val history = this.history
        if (history != null) {
            val stockPrice = history[position].item
            holder.binding.stockPrice = stockPrice
        }
        else {
            holder.binding.tvTime.text = ""
            holder.binding.tvPrice.text = ""
        }
    }

    override fun getItemCount(): Int {
        val history = this.history
        return history?.size ?: 0
    }

}
