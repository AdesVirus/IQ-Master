package com.example.iqmaster

import com.example.iqmaster.data.local.HighScoreStorage
import com.example.iqmaster.data.model.HighScore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import android.content.Context
import android.content.SharedPreferences

/**
 * Unit tests for HighScoreStorage
 */
class HighScoreStorageTest {
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var highScoreStorage: HighScoreStorage
    
    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)
        
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        
        highScoreStorage = HighScoreStorage(mockContext)
    }
    
    @Test
    fun testGetScores_EmptyByDefault() {
        `when`(mockSharedPreferences.getString("high_scores", null)).thenReturn(null)
        
        val scores = highScoreStorage.getScores()
        assertTrue(scores.isEmpty())
    }
    
    @Test
    fun testSaveScore() {
        `when`(mockSharedPreferences.getString("high_scores", null)).thenReturn(null)
        
        highScoreStorage.saveScore(100)
        
        verify(mockEditor).putString(eq("high_scores"), anyString())
        verify(mockEditor).apply()
    }
    
    @Test
    fun testClearScores() {
        highScoreStorage.clearScores()
        
        verify(mockEditor).remove("high_scores")
        verify(mockEditor).apply()
    }
    
    @Test
    fun testMergeWithRemoteScores() {
        `when`(mockSharedPreferences.getString("high_scores", null)).thenReturn(null)
        
        val remoteScores = listOf(
            HighScore(id = "1", score = 100, localTimestamp = 1000L),
            HighScore(id = "2", score = 90, localTimestamp = 2000L)
        )
        
        val merged = highScoreStorage.mergeWithRemoteScores(remoteScores)
        
        // Should return sorted by score descending
        assertEquals(2, merged.size)
        assertEquals(100, merged[0].score)
        assertEquals(90, merged[1].score)
    }
    
    @Test
    fun testMergeWithRemoteScores_Deduplication() {
        val score1 = HighScore(id = "1", score = 100, localTimestamp = 1000L)
        val score2 = HighScore(id = "1", score = 100, localTimestamp = 1000L) // Duplicate
        
        // Mock getScores to return score1
        val json = """[{"id":"1","score":100,"localTimestamp":1000}]"""
        `when`(mockSharedPreferences.getString("high_scores", null)).thenReturn(json)
        
        val remoteScores = listOf(score2)
        val merged = highScoreStorage.mergeWithRemoteScores(remoteScores)
        
        // Should deduplicate by ID
        assertEquals(1, merged.size)
    }
    
    @Test
    fun testScoreLimit() {
        `when`(mockSharedPreferences.getString("high_scores", null)).thenReturn(null)
        
        // Create 25 scores (more than MAX_SCORES = 20)
        val remoteScores = (1..25).map { i ->
            HighScore(id = "score_$i", score = i, localTimestamp = i.toLong())
        }
        
        val merged = highScoreStorage.mergeWithRemoteScores(remoteScores)
        
        // Should limit to top 20 scores
        assertEquals(20, merged.size)
        // Should be sorted by score descending
        assertEquals(25, merged[0].score)
        assertEquals(6, merged[19].score)
    }
}
