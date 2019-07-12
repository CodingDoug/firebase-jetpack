/*
 * Copyright 2019 Google LLC
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

import androidx.arch.core.util.Function
import com.hyperaware.android.firebasejetpack.common.DataOrException
import com.hyperaware.android.firebasejetpack.livedata.rtdb.DataSnapshotOrException
import com.hyperaware.android.firebasejetpack.repo.Deserializer

internal class DeserializeDataSnapshotTransform<T>(
    private val deserializer: DataSnapshotDeserializer<T>
) : Function<DataSnapshotOrException, DataOrException<T, Exception>> {

    override fun apply(input: DataSnapshotOrException): DataOrException<T, Exception> {
        val (snapshot, exception) = input
        return when {
            snapshot != null -> try {
                val value = deserializer.deserialize(snapshot)
                DataOrException(value, null)
            }
            catch (e: Deserializer.DeserializerException) {
                DataOrException(null, e)
            }

            exception != null -> DataOrException(null, exception)

            else -> DataOrException(null, Exception())
        }
    }

}
