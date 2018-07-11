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

package com.hyperaware.android.firebasejetpack.activity.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.hyperaware.android.firebasejetpack.R
import com.hyperaware.android.firebasejetpack.activity.livepricehistory.StockPriceHistoryActivity
import com.hyperaware.android.firebasejetpack.activity.multitracker.StockPriceTrackerActivity
import com.hyperaware.android.firebasejetpack.activity.multitrackerrv.StockPriceTrackerRecyclerViewActivity
import com.hyperaware.android.firebasejetpack.koin.RuntimeConfig
import com.hyperaware.android.firebasejetpack.repo.firestore.FirestoreStockRepository
import com.hyperaware.android.firebasejetpack.repo.rtdb.RealtimeDatabaseStockRepository
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = Activity.RESULT_FIRST_USER

        private enum class Database { Firestore, RealtimeDatabase }
        private var database = Database.Firestore
    }

    private val auth by inject<FirebaseAuth>()
    private val runtimeConfig by inject<RuntimeConfig>()

    private lateinit var vRoot: View
    private lateinit var vToolbar: Toolbar
    private lateinit var btnLogIn: Button
    private lateinit var btnLogOut: Button
    private lateinit var btnToggleDb: Button
    private lateinit var btnTrackTwo: Button
    private lateinit var btnTrackRecyclerView: Button
    private lateinit var btnStockHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                snack("Signed in")
            }
            else {
                // Sign in failed
                val response = IdpResponse.fromResultIntent(data) ?: return

                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    snack("No network")
                    return
                }

                snack("Unknown error with sign in")
                Log.e(TAG, "Sign-in error: ", response.error)
            }
        }
    }

    private fun initViews() {
        setContentView(R.layout.activity_main)

        vRoot = findViewById(R.id.root)

        initActionBar()

        btnLogIn = findViewById(R.id.btn_log_in)
        btnLogIn.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                    .build(),
                RC_SIGN_IN
            )
        }

        btnLogOut = findViewById(R.id.btn_log_out)
        btnLogOut.setOnClickListener {
            AuthUI.getInstance().signOut(this)
        }

        btnToggleDb = findViewById(R.id.btn_toggle_db)
        updateToggleButton()
        btnToggleDb.setOnClickListener {
            toggleDatabase()
        }

        btnTrackTwo = findViewById(R.id.btn_track_two_stocks)
        btnTrackTwo.setOnClickListener {
            startActivity(Intent(this, StockPriceTrackerActivity::class.java))
        }

        btnTrackRecyclerView = findViewById(R.id.btn_track_recycler_view)
        btnTrackRecyclerView.setOnClickListener {
            startActivity(Intent(this, StockPriceTrackerRecyclerViewActivity::class.java))
        }

        btnStockHistory = findViewById(R.id.btn_stock_history)
        btnStockHistory.setOnClickListener {
            startActivity(Intent(this, StockPriceHistoryActivity::class.java))
        }
    }

    private fun updateToggleButton() {
        val label = when (database) {
            Database.Firestore -> "Switch to Realtime Database"
            Database.RealtimeDatabase -> "Switch to Firestore"
        }
        btnToggleDb.text = label
    }

    private fun toggleDatabase() {
        when (database) {
            Database.Firestore -> {
                runtimeConfig.stockRepository = inject<RealtimeDatabaseStockRepository>().value
                database = Database.RealtimeDatabase
            }
            Database.RealtimeDatabase -> {
                runtimeConfig.stockRepository = inject<FirestoreStockRepository>().value
                database = Database.Firestore
            }
        }
        updateToggleButton()
    }

    private fun initActionBar() {
        vToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(vToolbar)
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val loggedIn = auth.currentUser != null
        btnLogIn.isEnabled = !loggedIn
        btnLogOut.isEnabled = loggedIn
        btnTrackTwo.isEnabled = loggedIn
        btnTrackRecyclerView.isEnabled = loggedIn
        btnStockHistory.isEnabled = loggedIn
//        if (loggedIn) {
//            startActivity(Intent(this, MultiTrackerRecyclerView::class.java))
//        }
    }

    private fun snack(message: String) {
        Snackbar.make(vRoot, message, Snackbar.LENGTH_SHORT).show()
    }

}
