package com.example.iqmaster.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.iqmaster.data.model.HighScore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Local storage for high scores using SharedPreferences
 */
class HighScoreStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "iq_master_scores"
        private const val KEY_HIGH_SCORES = "high_scores"
        private const val MAX_SCORES = 20
    }
    
    /**
     * Save a new high score
     */
    fun saveScore(score: Int) {
        val scores = getScores().toMutableList()
        val newScore = HighScore(
            id = System.currentTimeMillis().toString(),
            score = score,
            localTimestamp = System.currentTimeMillis()
        )
        scores.add(newScore)
        
        // Sort by score descending, then by timestamp
        val sortedScores = scores
            .sortedWith(compareByDescending<HighScore> { it.score }
                .thenByDescending { it.localTimestamp })
            .take(MAX_SCORES)
        
        saveScores(sortedScores)
    }
    
    /**
     * Get all local high scores
     */
    fun getScores(): List<HighScore> {
        val json = prefs.getString(KEY_HIGH_SCORES, null) ?: return emptyList()
        val type = object : TypeToken<List<HighScore>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Save list of scores
     */
    private fun saveScores(scores: List<HighScore>) {
        val json = gson.toJson(scores)
        prefs.edit().putString(KEY_HIGH_SCORES, json).apply()
    }
    
    /**
     * Clear all local scores
     */
    fun clearScores() {
        prefs.edit().remove(KEY_HIGH_SCORES).apply()
    }
    
    /**
     * Merge remote scores with local ones
     */
    fun mergeWithRemoteScores(remoteScores: List<HighScore>): List<HighScore> {
        val localScores = getScores()
        val allScores = (localScores + remoteScores).distinctBy { it.id }
        
        return allScores
            .sortedWith(compareByDescending<HighScore> { it.score }
                .thenByDescending { it.localTimestamp })
            .take(MAX_SCORES)
    }
}
