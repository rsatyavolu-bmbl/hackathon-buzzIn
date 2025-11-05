package com.buzzin.app

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.configuration.AmplifyOutputs
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
        // TODO: Uncomment when amplify_outputs.json is available
        // configureAmplify()

        // Initialize other SDKs as needed
    }

    private fun configureAmplify() {
        try {
            // Only add API plugin for now (no auth, no S3 for MVP)
            Amplify.addPlugin(AWSApiPlugin())
            // Gen 2 configuration using amplify_outputs.json
            // TODO: Uncomment when amplify_outputs.json is available in res/raw/
            // Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)
            Log.i(TAG, "Initialized Amplify Gen 2 successfully")
        } catch (error: AmplifyException) {
            Log.e(TAG, "Could not initialize Amplify", error)
            Log.e(TAG, "Make sure amplify_outputs.json is in res/raw/")
        }
    }

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

