package com.example.iqmaster.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Consent record for audit trail in Firestore
 */
data class ConsentRecord(
    val consentType: String = "", // "personalized", "non-personalized", "no-ads"
    val consentStatus: String = "", // UMP consent status
    @ServerTimestamp
    val timestamp: Timestamp? = null
) {
    constructor() : this("", "", null)
    
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "consentType" to consentType,
            "consentStatus" to consentStatus,
            "timestamp" to timestamp
        )
    }
}
