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

//
// This is a command line program that advances the state of StockMachine
// by calling its onTick method some number of times.  It will advance
// forever by default, or with a number of ticks given in the --ticks flag.
//

require('source-map-support').install()

import * as admin from 'firebase-admin'
import { StockMachine } from './stocks/stock-machine'

const PERIOD = 1000

interface Options {
    ticks: number
}

const args = require('command-line-args')
const options: Options = args([
    { name: 'ticks', alias: 't', type: Number }
])

let ticks = options.ticks
const forever = !(ticks > 0)

const serviceAccount = require('../service-account.json')
const app = admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: `https://${serviceAccount.project_id}.firebaseio.com`
})

// Needed temporarily to kill the warning about timestamps
app.firestore().settings({ timestampsInSnapshots: true })

async function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function go() {
    const machine = new StockMachine(app)
    let lastTick
    while (ticks-- > 0 || forever) {
        lastTick = Date.now()
        console.log("tick")
        await machine.onTick()
        await sleep(PERIOD - (Date.now() - lastTick))
    }
}

go().then(() => {
    process.exit(0)
})
.catch(err => {
    console.error(err)
    process.exit(1)
})
