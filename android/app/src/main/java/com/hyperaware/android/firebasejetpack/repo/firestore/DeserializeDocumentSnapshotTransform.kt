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

package com.hyperaware.android.firebasejetpack.repo.firestore

import androidx.arch.core.util.Function
import com.hyperaware.android.firebasejetpack.common.DataOrException
import com.hyperaware.android.firebasejetpack.livedata.firestore.DocumentSnapshotOrException
import com.hyperaware.android.firebasejetpack.repo.Deserializer

internal class DeserializeDocumentSnapshotTransform<T>(
    private val deserializer: DocumentSnapshotDeserializer<T>
) : Function<DocumentSnapshotOrException, DataOrException<T, Exception>> {

    override fun apply(input: DocumentSnapshotOrException): DataOrException<T, Exception> {
        val (snapshot, exception) = input
        return when {
            snapshot != null -> try {
                val data = deserializer.deserialize(snapshot)
                DataOrException(data, null)
            }
            catch (e: Deserializer.DeserializerException) {
                DataOrException(null, e)
            }

            exception != null -> DataOrException(null, exception)

            else -> DataOrException(null, Exception("Both data and exception were null"))
        }
    }

}
