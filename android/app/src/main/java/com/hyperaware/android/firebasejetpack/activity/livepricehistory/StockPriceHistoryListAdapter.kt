package com.hyperaware.android.firebasejetpack.activity.livepricehistory

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import com.hyperaware.android.firebasejetpack.databinding.HistoricalPriceListItemBinding
import com.hyperaware.android.firebasejetpack.repo.QueryItem
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceDisplay
import com.hyperaware.android.firebasejetpack.viewmodel.stockPriceDisplayDiffCallback
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * This ListAdapter maps a QueryItem<StockPriceDisplay> data object into a
 * HistoricalPriceViewHolder via data binding in historical_price_list_item.
 */

internal class StockPriceHistoryListAdapter
    : ListAdapter<QueryItem<StockPriceDisplay>, HistoricalPriceViewHolder>(asyncDifferConfig) {

    companion object : KoinComponent {
        private val executors by inject<AppExecutors>()
        private val asyncDifferConfig =
            AsyncDifferConfig.Builder<QueryItem<StockPriceDisplay>>(stockPriceDisplayDiffCallback)
                .setBackgroundThreadExecutor(executors.cpuExecutorService)
                .build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricalPriceViewHolder {
        // Using data binding on the individual views
        val inflater = LayoutInflater.from(parent.context)
        val binding = HistoricalPriceListItemBinding.inflate(inflater, parent, false)
        return HistoricalPriceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoricalPriceViewHolder, position: Int) {
        val qItem = getItem(position)
        holder.binding.stockPrice = qItem.item
    }

}
