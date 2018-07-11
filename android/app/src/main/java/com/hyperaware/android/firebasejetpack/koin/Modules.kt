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

package com.hyperaware.android.firebasejetpack.koin

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import com.hyperaware.android.firebasejetpack.repo.firestore.FirestoreStockRepository
import com.hyperaware.android.firebasejetpack.repo.StockRepository
import com.hyperaware.android.firebasejetpack.repo.rtdb.RealtimeDatabaseStockRepository
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext

interface RuntimeConfig {
    var stockRepository: StockRepository
}

class SingletonRuntimeConfig : RuntimeConfig {
    companion object {
        val instance = SingletonRuntimeConfig()
    }

    override var stockRepository: StockRepository = firestoreStockRepository
}

private val firestoreStockRepository by lazy { FirestoreStockRepository() }
private val realtimeDatabaseStockRepository by lazy { RealtimeDatabaseStockRepository() }

val appModule: Module = applicationContext {
    bean { firestoreStockRepository }
    bean { realtimeDatabaseStockRepository }
    bean { SingletonRuntimeConfig.instance as RuntimeConfig }
    factory { get<RuntimeConfig>().stockRepository }
    bean { AppExecutors.instance }
}

val firebaseModule: Module = applicationContext {
    bean { FirebaseAuth.getInstance() }
    bean { FirebaseFirestore.getInstance() }
    bean {
        val instance = FirebaseDatabase.getInstance()
        instance.setPersistenceEnabled(false)
        instance
    }
}

val allModules = listOf(appModule, firebaseModule)
