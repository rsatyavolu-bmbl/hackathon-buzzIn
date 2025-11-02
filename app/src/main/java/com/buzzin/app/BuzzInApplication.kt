package com.buzzin.app

import android.app.Application
import android.util.Log

class BuzzInApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BuzzIn Application starting...")
        
        // Initialize AWS Amplify
        // configureAmplify()
        
        // Initialize other SDKs as needed
    }

    /*
    private fun configureAmplify() {
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSLocationGeoPlugin())
            Amplify.configure(applicationContext)
            Log.i(TAG, "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e(TAG, "Could not initialize Amplify", error)
        }
    }
    */

    companion object {
        private const val TAG = "BuzzInApplication"
    }
}

