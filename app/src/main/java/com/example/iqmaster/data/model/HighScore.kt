package com.example.iqmaster.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * High score data model for local and Firestore storage
 */
data class HighScore(
    @DocumentId
    val id: String = "",
    val score: Int = 0,
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val localTimestamp: Long = System.currentTimeMillis()
) {
    // No-arg constructor for Firestore
    constructor() : this("", 0, null, System.currentTimeMillis())
    
    companion object {
        fun fromMap(map: Map<String, Any>): HighScore {
            return HighScore(
                id = map["id"] as? String ?: "",
                score = (map["score"] as? Long)?.toInt() ?: 0,
                timestamp = map["timestamp"] as? Timestamp,
                localTimestamp = map["localTimestamp"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
    
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "score" to score,
            "timestamp" to timestamp,
            "localTimestamp" to localTimestamp
        )
    }
}
