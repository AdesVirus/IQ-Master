package com.example.iqmaster.data.repository

import android.util.Log
import com.example.iqmaster.data.local.HighScoreStorage
import com.example.iqmaster.data.model.HighScore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for managing high scores with local and Firestore sync
 */
class HighScoreRepository(
    private val localStorage: HighScoreStorage,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "HighScoreRepository"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_SCORES = "scores"
    }
    
    /**
     * Save score locally and sync to Firestore
     */
    suspend fun saveScore(score: Int): Result<Unit> {
        return try {
            // Save locally first
            localStorage.saveScore(score)
            
            // Sync to Firestore if user is authenticated
            val userId = auth.currentUser?.uid
            if (userId != null) {
                syncScoreToFirestore(userId, score)
            } else {
                Log.w(TAG, "User not authenticated, score saved locally only")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving score", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync score to Firestore
     */
    private suspend fun syncScoreToFirestore(userId: String, score: Int) {
        try {
            val scoreData = HighScore(
                score = score,
                localTimestamp = System.currentTimeMillis()
            )
            
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SCORES)
                .add(scoreData.toMap())
                .await()
            
            Log.d(TAG, "Score synced to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing score to Firestore", e)
            // Don't throw, score is already saved locally
        }
    }
    
    /**
     * Get all scores (local + remote merged)
     */
    fun getAllScores(): Flow<List<HighScore>> = flow {
        try {
            val localScores = localStorage.getScores()
            
            // Try to fetch remote scores
            val userId = auth.currentUser?.uid
            if (userId != null) {
                try {
                    val remoteScores = fetchRemoteScores(userId)
                    val merged = localStorage.mergeWithRemoteScores(remoteScores)
                    emit(merged)
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching remote scores", e)
                    emit(localScores)
                }
            } else {
                emit(localScores)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting scores", e)
            emit(emptyList())
        }
    }
    
    /**
     * Fetch scores from Firestore
     */
    private suspend fun fetchRemoteScores(userId: String): List<HighScore> {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SCORES)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    HighScore(
                        id = doc.id,
                        score = doc.getLong("score")?.toInt() ?: 0,
                        timestamp = doc.getTimestamp("timestamp"),
                        localTimestamp = doc.getLong("localTimestamp") ?: 0L
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing score document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching remote scores", e)
            emptyList()
        }
    }
    
    /**
     * Clear local scores
     */
    fun clearLocalScores() {
        localStorage.clearScores()
    }
    
    /**
     * Clear remote scores
     */
    suspend fun clearRemoteScores(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )
            
            val scoresRef = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SCORES)
            
            val snapshot = scoresRef.get().await()
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            Log.d(TAG, "Remote scores cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing remote scores", e)
            Result.failure(e)
        }
    }
}
