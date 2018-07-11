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

import { FirestoreStockRepository } from "./firestore-repo"
import { RealtimeDatabaseStockRepository } from "./database-repo"
import { StockPrice } from "./repo"
import { app } from "firebase-admin"

/**
 * This class implements a stock market machine that decides, at each
 * progressive "tick", the direction an magnitude of each stock known
 * stock ticker.
 *
 * The state of the market is recorded in both Firestore and Realtime
 * Database, but Firestore is considered the "master" for the purpose
 * of determining current state.
 */

 export class StockMachine {

    private static readonly ALL_TICKERS = [
        "HSTK", "FBAS",
        "QIX",  "GORF", "ZAXN", "PCMN", "GLXN",
        "VGTA", "GOKU", "BLMA", "GOHN", "FRZA", "CELL", "BUU",
        "MCOY", "SPOK", "KIRK", "UHRA", "CHKV", "SULU"
    ]

    private firestoreRepo: FirestoreStockRepository
    private databaseRepo: RealtimeDatabaseStockRepository

    constructor(ap: app.App) {
        this.firestoreRepo = new FirestoreStockRepository(ap.firestore())
        this.databaseRepo = new RealtimeDatabaseStockRepository(ap.database())
    }

    async onTick(): Promise<any> {
        // Update each stock asynchronously
        const promises = StockMachine.ALL_TICKERS.map(ticker => {
            return this.updateStock(ticker)
        })

        return Promise.all(promises)
    }

    private async updateStock(ticker: string): Promise<any> {
        const now = new Date()
        let stock = await this.firestoreRepo.getStockLive(ticker)
        let currentPrice
        if (stock) {
            currentPrice = stock.price
            this.advanceStockPrice(stock)
            stock.time = now
        }
        else {
            currentPrice = 0
            stock = {
                price: 10,
                time: now
            }
        }
        if (currentPrice !== stock.price) {
            return Promise.all([
                this.firestoreRepo.updateStockPrice(ticker, stock),
                this.databaseRepo.updateStockPrice(ticker, stock)
            ])
        }
        else {
            return null
        }
    }

    private advanceStockPrice(stock: StockPrice) {
        const direction = Math.random()
        const magnitude = Math.random() / 90

        if (direction < .01) {
            stock.price -= stock.price * .1
        }
        else if (direction < .35) {
            stock.price += stock.price * magnitude
        }
        else if (direction < .45) {
            stock.price -= stock.price * magnitude
        }

        if (stock.price < .1) { stock.price = .1 }
    }

}
