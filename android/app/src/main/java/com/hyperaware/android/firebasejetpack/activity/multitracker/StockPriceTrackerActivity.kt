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

package com.hyperaware.android.firebasejetpack.activity.multitracker

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.hyperaware.android.firebasejetpack.R
import com.hyperaware.android.firebasejetpack.activity.livepricehistory.StockPriceHistoryActivity
import com.hyperaware.android.firebasejetpack.databinding.StockPriceListItemBinding
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceDisplay
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceViewModel
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject

class StockPriceTrackerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "StockPriceTrackerAct"
        private val TICKERS = sortedSetOf("HSTK", "FBAS")
    }

    private val auth by inject<FirebaseAuth>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The root view/scaffolding
        setContentView(R.layout.activity_multi_tracker)
        toolbar.setTitle(R.string.track_stocks)
        val container = findViewById<ViewGroup>(R.id.c_stocks)

        // The model
        val stocksViewModel = ViewModelProviders.of(this).get(StockPriceViewModel::class.java)

        TICKERS.forEach { ticker ->
            // The stock views
            val binding = StockPriceListItemBinding.inflate(layoutInflater, container, true)

            // Wire them up
            val liveData = stocksViewModel.getStockLiveData(ticker)
            liveData.observe(this, Observer { stockPriceDisplay ->
                if (stockPriceDisplay != null) {
                    if (stockPriceDisplay.data != null) {
                        binding.stockPrice = stockPriceDisplay.data
                    }
                    else if (stockPriceDisplay.exception != null) {
                        Log.e(TAG, "Observed unexpected exception", stockPriceDisplay.exception)
                        binding.stockPrice = StockPriceDisplay(ticker, "ERR", "")
                    }
                }
            })

            binding.root.setOnClickListener {
                val intent = StockPriceHistoryActivity.newIntent(this, ticker)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser == null) {
            finish()
        }
    }

}
