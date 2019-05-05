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

import com.google.firebase.database.DataSnapshot
import com.hyperaware.android.firebasejetpack.model.StockPrice
import com.hyperaware.android.firebasejetpack.repo.Deserializer
import java.util.*

internal interface DataSnapshotDeserializer<T> : Deserializer<DataSnapshot, T>

internal class StockPriceSnapshotDeserializer : DataSnapshotDeserializer<StockPrice> {
    override fun deserialize(input: DataSnapshot): StockPrice {
        val data = input.value
        return if (data is Map<*, *>) {
            val ticker = input.key!!

            val price = data["price"] ?:
                throw Deserializer.DeserializerException("price was missing for stock price snapshot $ticker")
            val priceFloat = if (price is Number) {
                price.toFloat()
            }
            else {
                throw Deserializer.DeserializerException("price not a float for stock price snapshot $ticker")
            }

            val time = data["time"] ?:
                throw Deserializer.DeserializerException("time was missing for stock price snapshot $ticker")
            val timeDate = if (time is Number) {
                Date(time.toLong())
            }
            else {
                throw Deserializer.DeserializerException("time not a number for stock price snapshot $ticker")
            }

            StockPrice(ticker, priceFloat, timeDate)
        }
        else {
            throw Deserializer.DeserializerException("DataSnapshot value wasn't an object Map")
        }
    }
}
