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

package com.hyperaware.android.firebasejetpack.activity.multitrackerrv

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hyperaware.android.firebasejetpack.databinding.StockPriceListItemBinding
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceDisplayOrException
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceViewModel

internal class StocksRecyclerViewAdapter(
    private val stockPriceViewModel: StockPriceViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val itemClickListener: ItemClickListener<StockViewHolder>
) : RecyclerView.Adapter<StockViewHolder>() {

    companion object {
        private const val TAG = "StocksRVAdapter"
    }

    private val allTickersList = stockPriceViewModel.allTickers.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        // Using data binding on the individual views
        val inflater = LayoutInflater.from(parent.context)
        val binding = StockPriceListItemBinding.inflate(inflater, parent, false)
        val holder = StockViewHolder(binding)
        holder.itemClickListener = itemClickListener
        return holder
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val ticker = allTickersList[position]
        holder.binding.tvTicker.text = ticker
        holder.binding.tvPrice.text = "..."

        // When a view gets bound, observe the stocks changes, and update the view
        val observer = Observer<StockPriceDisplayOrException> { stockPriceDisplay ->
            if (stockPriceDisplay != null) {
                if (stockPriceDisplay.data != null) {
                    holder.binding.stockPrice = stockPriceDisplay.data
                }
                else if (stockPriceDisplay.exception != null) {
                    Log.e(TAG, "Observed unexpected exception", stockPriceDisplay.exception)
                    TODO("Handle the error")
                }
            }
        }

        val stockLiveData = stockPriceViewModel.getStockLiveData(ticker)
        stockLiveData.observe(lifecycleOwner, observer)

        holder.ticker = ticker
        holder.stockPriceLiveData = stockLiveData
        holder.observer = observer
    }

    override fun onViewRecycled(holder: StockViewHolder) {
        super.onViewRecycled(holder)
        // When a view scrolls away, stop observing the stock changes
        holder.stockPriceLiveData!!.removeObserver(holder.observer!!)
        holder.stockPriceLiveData = null
        holder.observer = null
    }

    override fun getItemCount(): Int {
        return allTickersList.size
    }

    internal interface ItemClickListener<StockViewHolder> {
        fun onItemClick(holder: StockViewHolder)
    }

}
