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
import { firestore } from 'firebase-admin'
import { CollectionReference } from '@google-cloud/firestore'

export class FirestoreStockRepository extends StockRepositoryBase {

    private firestore: firestore.Firestore
    private stocksLiveCollection: CollectionReference

    constructor(fstore: firestore.Firestore) {
        super()
        this.firestore = fstore
        this.stocksLiveCollection = this.firestore.collection('stocks-live')
    }

    async getStockLive(ticker: string): Promise<StockPrice | null> {
        const snap = await this.stocksLiveCollection.doc(ticker).get()
        if (snap.exists) {
            const price = snap.data() as StockPrice
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
        const stockDoc = this.stocksLiveCollection.doc(ticker)
        const p1 = stockDoc.set(stockPrice, { merge: true })

        const historyColl = stockDoc.collection('recent-history')
        const historyId = stockPrice.time.getTime().toString()
        const historyDoc = historyColl.doc(historyId)
        const p2 = historyDoc.set(stockPrice)

        const p3 = this.deleteOldHistory(historyColl)

        return Promise.all([p1, p2, p3])
    }

    private async deleteOldHistory(historyColl: CollectionReference): Promise<any> {
        const expired = new Date(Date.now() - this.historyExpires)
        const snapshot = await historyColl.where('time', '<', expired).get()
        return snapshot.docs.map(snap => snap.ref.delete())
    }

}
