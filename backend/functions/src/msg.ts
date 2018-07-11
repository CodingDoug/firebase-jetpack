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
 // Command line program that uses the Firebase Admin SDK to send a single
 // topic message to the ticker given in the --ticker flag.
 //

require('source-map-support').install()

import * as admin from 'firebase-admin'

interface Options {
    ticker: string
}

const args = require('command-line-args')
const options: Options = args([
    { name: 'ticker', alias: 't', type: String }
])

if (!options.ticker) {
    console.error("--ticker is required")
    process.exit(1)
}

const serviceAccount = require('../service-account.json')
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
})

const payload = {
    data: {
        ticker: options.ticker
    }
}

admin.messaging().sendToTopic(options.ticker, payload)
.then(response => {
    console.log(response)
    process.exit(0)
})
.catch(error => {
    console.log(error)
    process.exit(1)
})
