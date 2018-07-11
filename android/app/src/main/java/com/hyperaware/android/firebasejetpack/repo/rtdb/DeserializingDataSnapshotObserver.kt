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

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import com.hyperaware.android.firebasejetpack.config.AppExecutors
import com.hyperaware.android.firebasejetpack.livedata.rtdb.DataSnapshotOrException
import com.hyperaware.android.firebasejetpack.common.DataOrException
import com.hyperaware.android.firebasejetpack.repo.Deserializer
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

internal class DeserializingDataSnapshotObserver<T>(
    private val deserializer: DataSnapshotDeserializer<T>,
    private val liveData: MediatorLiveData<DataOrException<T, Exception>>
) : Observer<DataSnapshotOrException>, KoinComponent {

    private val executors by inject<AppExecutors>()

    override fun onChanged(result: DataSnapshotOrException?) {
        if (result != null) {
            val snapshot = result.data
            val exception = result.exception
            if (snapshot != null) {
                // Do this in a thread because DataSnapshot
                // deserialization with reflection can be costly.
                executors.cpuExecutorService.execute {
                    try {
                        val value = deserializer.deserialize(snapshot)
                        liveData.postValue(DataOrException(value, null))
                    }
                    catch (e: Deserializer.DeserializerException) {
                        liveData.postValue(DataOrException(null, e))
                    }
                }
            }
            else if (exception != null) {
                liveData.value = DataOrException(null, exception)
            }
        }
        else {
            liveData.value = null
        }
    }

}
