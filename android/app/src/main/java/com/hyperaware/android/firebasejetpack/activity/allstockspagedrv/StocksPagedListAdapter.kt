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

package com.hyperaware.android.firebasejetpack.activity.allstockspagedrv

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.paging.PagedListAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hyperaware.android.firebasejetpack.databinding.StockPriceListItemBinding
import com.hyperaware.android.firebasejetpack.diffcallback.QueryItemOrExceptionDiffUtilItemCallback
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.QueryItemOrException
import com.hyperaware.android.firebasejetpack.viewmodel.PagedStockPricesViewModel
import com.hyperaware.android.firebasejetpack.viewmodel.toStockPriceDisplay

internal class StocksPagedListAdapter(
    stockPriceViewModel: PagedStockPricesViewModel,
    lifecycleOwner: LifecycleOwner,
    private val itemClickListener: ItemClickListener<StockViewHolder>
) : PagedListAdapter<QueryItemOrException<StockPrice>, StockViewHolder>(QueryItemOrExceptionDiffUtilItemCallback()) {

    init {
        stockPriceViewModel.getAllStockPricesPagedListLiveData().observe(lifecycleOwner, Observer {
            submitList(it)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        // Using data binding on the individual views
        val inflater = LayoutInflater.from(parent.context)
        val binding = StockPriceListItemBinding.inflate(inflater, parent, false)
        val holder = StockViewHolder(binding)
        holder.itemClickListener = itemClickListener
        return holder
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = getItem(position)
        if (item?.data != null) {
            val ticker = item.data.item.ticker
            val display = item.data.item.toStockPriceDisplay()
            holder.binding.stockPrice = display
            holder.ticker = ticker
        }
    }

    internal interface ItemClickListener<StockViewHolder> {
        fun onItemClick(holder: StockViewHolder)
    }

}
