package com.example.iqmaster.consent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.iqmaster.BuildConfig
import com.example.iqmaster.data.local.ConsentStorage
import com.example.iqmaster.data.model.ConsentRecord
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Manager for User Messaging Platform (UMP) consent
 */
class UMPConsentManager(
    private val context: Context,
    private val consentStorage: ConsentStorage,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val consentInformation: ConsentInformation = 
        UserMessagingPlatform.getConsentInformation(context)
    
    companion object {
        private const val TAG = "UMPConsentManager"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CONSENT = "consent"
    }
    
    /**
     * Request consent information update
     */
    suspend fun requestConsentInfoUpdate(
        activity: Activity,
        debugEnabled: Boolean = BuildConfig.UMP_DEBUG_ENABLED,
        testDeviceHashedId: String = BuildConfig.TEST_DEVICE_HASHED_ID
    ): Result<Unit> {
        return try {
            val paramsBuilder = ConsentRequestParameters.Builder()
            
            // Add debug settings if enabled
            if (debugEnabled) {
                val debugSettings = ConsentDebugSettings.Builder(context)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .setForceTesting(true)
                
                // Add test device if provided
                if (testDeviceHashedId.isNotEmpty()) {
                    debugSettings.addTestDeviceHashedId(testDeviceHashedId)
                    Log.d(TAG, "UMP Debug Mode: Test device added: $testDeviceHashedId")
                }
                
                paramsBuilder.setConsentDebugSettings(debugSettings.build())
                Log.d(TAG, "UMP Debug Mode: ENABLED (simulating EEA)")
            }
            
            val params = paramsBuilder.build()
            
            // Request consent information
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    Log.d(TAG, "Consent info updated successfully")
                    consentStorage.saveUmpConsentStatus(consentInformation.consentStatus)
                    
                    // Show form if required
                    if (consentInformation.isConsentFormAvailable) {
                        Log.d(TAG, "Consent form is available")
                    }
                },
                { formError ->
                    Log.e(TAG, "Consent info update failed: ${formError.message}")
                }
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting consent info", e)
            Result.failure(e)
        }
    }
    
    /**
     * Load and show consent form if available
     */
    fun loadAndShowConsentFormIfRequired(
        activity: Activity,
        onConsentGathered: () -> Unit
    ) {
        if (consentInformation.isConsentFormAvailable) {
            loadConsentForm(activity) { loadSuccess ->
                if (loadSuccess) {
                    showConsentForm(activity, onConsentGathered)
                } else {
                    onConsentGathered()
                }
            }
        } else {
            Log.d(TAG, "Consent form not available")
            onConsentGathered()
        }
    }
    
    /**
     * Load consent form
     */
    private fun loadConsentForm(activity: Activity, onLoaded: (Boolean) -> Unit) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm ->
                Log.d(TAG, "Consent form loaded successfully")
                onLoaded(true)
            },
            { formError ->
                Log.e(TAG, "Consent form load failed: ${formError.message}")
                onLoaded(false)
            }
        )
    }
    
    /**
     * Show consent form
     */
    private fun showConsentForm(activity: Activity, onDismissed: () -> Unit) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
            activity
        ) { formError ->
            if (formError != null) {
                Log.e(TAG, "Consent form error: ${formError.message}")
            } else {
                Log.d(TAG, "Consent form dismissed")
                consentStorage.saveUmpConsentStatus(consentInformation.consentStatus)
                
                // Record consent in Firestore
                recordConsentToFirestore()
            }
            onDismissed()
        }
    }
    
    /**
     * Reset consent (for testing or user request)
     */
    fun resetConsent() {
        consentInformation.reset()
        consentStorage.clearConsent()
        Log.d(TAG, "Consent reset")
    }
    
    /**
     * Check if can request ads
     */
    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }
    
    /**
     * Get consent status
     */
    fun getConsentStatus(): Int {
        return consentInformation.consentStatus
    }
    
    /**
     * Get consent status string
     */
    fun getConsentStatusString(): String {
        return when (consentInformation.consentStatus) {
            ConsentInformation.ConsentStatus.UNKNOWN -> "Unknown"
            ConsentInformation.ConsentStatus.REQUIRED -> "Required"
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> "Not Required"
            ConsentInformation.ConsentStatus.OBTAINED -> "Obtained"
            else -> "Unknown"
        }
    }
    
    /**
     * Record consent to Firestore for audit
     */
    private fun recordConsentToFirestore() {
        try {
            val userId = auth.currentUser?.uid ?: return
            val consentType = consentStorage.getConsentType()
            val consentStatus = getConsentStatusString()
            
            val consentRecord = ConsentRecord(
                consentType = consentType,
                consentStatus = consentStatus
            )
            
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CONSENT)
                .add(consentRecord.toMap())
                .addOnSuccessListener {
                    Log.d(TAG, "Consent record saved to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving consent record", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error recording consent", e)
        }
    }
}
