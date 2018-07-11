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

package com.hyperaware.android.firebasejetpack.livedata.tasks

import android.arch.lifecycle.LiveData
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.hyperaware.android.firebasejetpack.common.DataOrException

/**
 * Converts a Play services Task to LiveData.  Currently unused.
 */

class TaskLiveData<T>(private val task: Task<T>)
    : LiveData<DataOrException<T, Exception>>(), OnSuccessListener<T>, OnFailureListener {

    private var added = false

    override fun onActive() {
        if (!added) {
            added = true
            task.addOnSuccessListener(this)
            task.addOnFailureListener(this)
        }
    }

    override fun onInactive() {
    }

    override fun onSuccess(result: T) {
        value = DataOrException(result, null)
    }

    override fun onFailure(exception: Exception) {
        value = DataOrException(null, exception)
    }

}
