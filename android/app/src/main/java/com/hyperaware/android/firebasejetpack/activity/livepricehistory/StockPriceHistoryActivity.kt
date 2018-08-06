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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.hyperaware.android.firebasejetpack.R
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceDisplayHistoryQueryResults
import com.hyperaware.android.firebasejetpack.viewmodel.StockPriceHistoryViewModel
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject

class StockPriceHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "StockPriceHistoryAct"
        const val EXTRA_TICKER = "StockPriceHistoryActivity.ticker"

        fun newIntent(context: Context, ticker: String): Intent {
            val intent = Intent(context, StockPriceHistoryActivity::class.java)
            intent.putExtra(StockPriceHistoryActivity.EXTRA_TICKER, ticker)
            return intent
        }
    }

    private val auth by inject<FirebaseAuth>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ticker = intent.getStringExtra(EXTRA_TICKER)
            ?: throw IllegalArgumentException("No ticker provided")

        // The model is a LiveData that contains a list of history items
        val priceHistoryViewModel = ViewModelProviders.of(this).get(StockPriceHistoryViewModel::class.java)

        // The root view/scaffolding
        setContentView(R.layout.activity_price_history)
        toolbar.title = "$ticker Recent History"

        findViewById<RecyclerView>(R.id.rv_price_history).apply {
            setHasFixedSize(true)
            layoutManager = MyLinearLayoutManager(this@StockPriceHistoryActivity)

            val listAdapter = StockPriceHistoryListAdapter()

            // Observe the stock price history changes, and submit them to the
            // ListAdapter.  Order of the list is reversed so that new data
            // appears at the top.
            //
            val historyObserver = Observer<StockPriceDisplayHistoryQueryResults> {
                if (it != null) {
                    if (it.data != null) {
                        listAdapter.submitList(it.data.reversed())
                    }
                    else if (it.exception != null) {
                        Log.e(TAG, "Error getting stock history", it.exception)
                        TODO("Handle the error")
                    }
                }
            }
            val historyLiveData = priceHistoryViewModel.getStockPriceHistory(ticker)
            historyLiveData.observe(this@StockPriceHistoryActivity, historyObserver)

            adapter = listAdapter
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


private class MyLinearLayoutManager(private val context: Context) : LinearLayoutManager(context) {

    // Force new items appear at the top
    override fun onItemsAdded(recyclerView: RecyclerView?, positionStart: Int, itemCount: Int) {
        super.onItemsAdded(recyclerView, positionStart, itemCount)
        scrollToPosition(0)
    }

    // Prevent scrolling (for now)
    override fun canScrollVertically(): Boolean {
        return false
    }

}
