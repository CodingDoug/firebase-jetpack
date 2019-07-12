package com.hyperaware.android.firebasejetpack.livedata.common

import androidx.lifecycle.LiveData
import android.os.Handler
import androidx.annotation.CallSuper
import android.util.Log

/**
 * A LiveData that allows its resource management to follow its active state,
 * but on a slight delay after inactivity, in order to allow expensive
 * resources to remain active during an Android configuration change.  For
 * example, if it's expensive or undesirable for a LiveData to detach and
 * reattach a listener to some resource because of a configuration change,
 * LingeringLiveData softens that cost by allowing it to keep that listener
 * attach during the change.  The downside is that the resource doesn't get
 * released at the very moment that it might no longer be removed.
 */

abstract class LingeringLiveData<T> : LiveData<T>() {

    companion object {
        private const val STOP_LISTENING_DELAY = 2000L
    }

    // To be fully unit-testable, this code should use an abstraction for
    // future work scheduling rather than Handler itself.
    private val handler = Handler()

    private var stopLingeringPending = false
    private val stopLingeringRunnable = StopLingeringRunnable()

    /**
     * Called during onActive, but only if it was not previously in a
     * "lingering" state.
     */
    abstract fun beginLingering()

    /**
     * Called two seconds after onInactive, but only if onActive is not
     * called during that time.
     */
    abstract fun endLingering()

    @CallSuper
    override fun onActive() {
        if (stopLingeringPending) {
            handler.removeCallbacks(stopLingeringRunnable)
        }
        else {
            beginLingering()
        }
        stopLingeringPending = false
    }

    @CallSuper
    override fun onInactive() {
        handler.postDelayed(stopLingeringRunnable, STOP_LISTENING_DELAY)
        stopLingeringPending = true
    }

    private inner class StopLingeringRunnable : Runnable {
        override fun run() {
            if (stopLingeringPending) {
                stopLingeringPending = false
                endLingering()
            }
        }
    }

}
