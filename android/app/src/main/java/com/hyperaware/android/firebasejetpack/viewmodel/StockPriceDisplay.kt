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

package com.hyperaware.android.firebasejetpack.viewmodel

import android.annotation.SuppressLint
import com.hyperaware.android.firebasejetpack.diffcallback.QueryItemDiffCallback
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.viewmodel.Formatters.priceFormatter
import com.hyperaware.android.firebasejetpack.viewmodel.Formatters.timeFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat

/**
 * A container class for displaying properly formatted stock price data.
 */

data class StockPriceDisplay(
    val ticker: String,
    val price: String,
    val time: String
)

/**
 * Converts a StockPrice object into a StockPriceDisplay object.
 */

fun StockPrice.toStockPriceDisplay() = StockPriceDisplay(
    this.ticker,
    priceFormatter.format(this.price),
    timeFormatter.format(this.time)
)

val stockPriceDisplayDiffCallback = object : QueryItemDiffCallback<StockPriceDisplay>() {}


@SuppressLint("SimpleDateFormat")
private object Formatters {

    val timeFormatter by lazy {
        SimpleDateFormat("HH:mm:ss")
    }

    val priceFormatter by lazy {
        val priceFormatter = NumberFormat.getNumberInstance()
        priceFormatter.minimumFractionDigits = 2
        priceFormatter.maximumFractionDigits = 2
        priceFormatter
    }

}
