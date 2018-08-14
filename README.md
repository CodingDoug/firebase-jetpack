# Firebase & Jetpack

This repo contains an Android app that demonstrates an (evolving) approach to
the use of Firebase along with some of Android's [Jetpack][2] components,
including ViewModel, LiveData, paging, data binding, and WorkManager.  Full
implementations are provided for [Realtime Database][3] and [Firestore][4].

## Live Demo

You can use a fully functioning version of this app by [installing it from the Play Store][1].

## Setup

Minimally, to get started:

1. Create a Firebase project
1. Enable Firestore and Realtime Database
1. Enable Google auth
1. Add add an Android app with the package name "com.hyperaware.android.firebasejetpack" and your SHA-1
1. Download google-services.json and place it in `android/app/google-services.json`
1. Download a service account from the console and place it in `backend/functions/service-account.json`
1. Initialize the backend code with `cd backend/functions; firebase init` and attach it to your project
1. Deploy the backend with `firebase deploy`
1. Run `node ./lib/tick.js --ticks 1` to bootstrap the database. Run with no args to tick infinitely.
1. Build and run the Android app to watch stock price changes in real time.

More details instructions and walkthrough coming later.

## License

The code in this project is licensed under the Apache License 2.0.

```text
Copyright 2018 Google LLC
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    https://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Disclaimer

This is not an officially supported Google product.


[1]: https://play.google.com/store/apps/details?id=com.hyperaware.android.firebasejetpack
[2]: https://developer.android.com/jetpack/
[3]: https://firebase.google.com/docs/database/
[4]: https://firebase.google.com/docs/firestore/
