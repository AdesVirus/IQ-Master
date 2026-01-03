package com.example.iqmaster

import com.example.iqmaster.data.local.ConsentStorage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import android.content.Context
import android.content.SharedPreferences

/**
 * Unit tests for ConsentStorage
 */
class ConsentStorageTest {
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var consentStorage: ConsentStorage
    
    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)
        
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        
        consentStorage = ConsentStorage(mockContext)
    }
    
    @Test
    fun testSaveConsentType() {
        consentStorage.saveConsentType(ConsentStorage.CONSENT_PERSONALIZED)
        verify(mockEditor).putString("consent_type", ConsentStorage.CONSENT_PERSONALIZED)
        verify(mockEditor).apply()
    }
    
    @Test
    fun testGetConsentType_Default() {
        `when`(mockSharedPreferences.getString("consent_type", ConsentStorage.CONSENT_PERSONALIZED))
            .thenReturn(ConsentStorage.CONSENT_PERSONALIZED)
        
        val result = consentStorage.getConsentType()
        assertEquals(ConsentStorage.CONSENT_PERSONALIZED, result)
    }
    
    @Test
    fun testSetNoAds_Enabled() {
        consentStorage.setNoAds(true)
        verify(mockEditor).putBoolean("no_ads", true)
        verify(mockEditor).putString("consent_type", ConsentStorage.CONSENT_NO_ADS)
    }
    
    @Test
    fun testIsNoAdsEnabled_Default() {
        `when`(mockSharedPreferences.getBoolean("no_ads", false)).thenReturn(false)
        
        val result = consentStorage.isNoAdsEnabled()
        assertFalse(result)
    }
    
    @Test
    fun testClearConsent() {
        consentStorage.clearConsent()
        verify(mockEditor).clear()
        verify(mockEditor).apply()
    }
}
