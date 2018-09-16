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

import { StockRepositoryBase, StockPrice } from './repo'
import { database } from 'firebase-admin'

export class RealtimeDatabaseStockRepository extends StockRepositoryBase {

    private database: database.Database
    private stocksLiveRef: database.Reference
    private stocksHistoryRef: database.Reference

    constructor(db: database.Database) {
        super()
        this.database = db
        this.stocksLiveRef = this.database.ref('stocks-live')
        this.stocksHistoryRef = this.database.ref('stocks-history')
    }

    async getStockLive(ticker: string): Promise<StockPrice | null> {
        const snap: database.DataSnapshot = await this.stocksLiveRef.child(ticker).once('value')
        if (snap.exists) {
            const price = snap.val() as StockPrice
            return {
                price: price.price,
                time: price.time
            }
        }
        else {
            return null
        }
    }

    async updateStockPrice(ticker: string, stockPrice: StockPrice): Promise<any> {
        const time = stockPrice.time.getTime()
        const price = {
            price: stockPrice.price,
            time: time
        }

        // Update current stock price
        const stockRef = this.stocksLiveRef.child(ticker)
        const p1 = stockRef.set(price)

        // Save stock price to history
        price['revTime'] = -time  // for reverse sorting by time
        const historyId = stockPrice.time.getTime().toString()
        const historyRef = this.stocksHistoryRef.child(ticker)
        const newHistoryRef = historyRef.child(historyId)
        const p2 = newHistoryRef.set(price)

        const p3 = this.deleteOldHistory(historyRef)

        return Promise.all([p1, p2, p3])
    }

    private async deleteOldHistory(historyRef: database.Reference): Promise<any> {
        const expired = Date.now() - this.historyExpires
        const snapshot: database.DataSnapshot =
            await historyRef.orderByChild('revTime').startAt(-expired).once('value')
        const snaps: database.Reference[] = []
        snapshot.forEach(snap => {
            snaps.push(snap.ref)
            return false
        })
        return Promise.all(snaps.map(ref => ref.remove()))
    }

}
