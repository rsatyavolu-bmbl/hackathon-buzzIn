package com.buzzin.app

import android.app.Application
import android.util.Log
import com.buzzin.app.data.DeviceIdManager

class BuzzInApplication : Application() {

    lateinit var deviceIdManager: DeviceIdManager
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BuzzIn Application starting...")
        
        // Initialize Device ID Manager
        deviceIdManager = DeviceIdManager.getInstance(this)
        val metadata = deviceIdManager.getDeviceMetadata(this)
        Log.d(TAG, "Application ID: ${metadata.applicationId}")
        Log.d(TAG, "Session ID: ${metadata.sessionId}")
        Log.d(TAG, "Android Device ID: ${metadata.androidDeviceId}")
        
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
        
        /**
         * Gets the DeviceIdManager instance from the application context.
         */
        fun getDeviceIdManager(application: Application): DeviceIdManager {
            return (application as BuzzInApplication).deviceIdManager
        }
    }
}

