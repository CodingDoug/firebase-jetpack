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

require('source-map-support').install()

import * as functions from 'firebase-functions'
import { initializeApp } from 'firebase-admin'
import { StockMachine } from './stocks/stock-machine'

const PERIOD = 1000  // 1 second between ticks

let machine  // lazy init

/*
 * This function implements a periodic tick system that advances the state
 * of the stock market "machine" implemented in StockMachine.  It uses
 * writes to RTDB to schedule the next run of this function.  The function
 * will likely execute faster than the period, so it will sleep for as long
 * as required to delay execution of the next tick.
 */

export const onStockMachineWrite = functions.database.ref('/machine').onWrite(async change => {
    const before = change.after.val()
    const after = change.after.val()
    if (!after) {
        console.log("/machine is empty")
        return null
    }

    if (!after.enabled) {
        console.log("machine is disabled")
        return null
    }

    const now = Date.now()
    const wait = PERIOD - (now - after.lastTick)
    if (wait > 0) {
        await sleep(wait)
    }

    if (!machine) {
        const app = initializeApp()
        machine = new StockMachine(app)
    }
    await machine.onTick()
    await change.after.ref.child('lastTick').set(now)

    return null
})


async function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}
