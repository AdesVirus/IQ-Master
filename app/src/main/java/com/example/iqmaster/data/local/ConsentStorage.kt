package com.example.iqmaster.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Storage for consent choices and preferences
 */
class ConsentStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "iq_master_consent"
        private const val KEY_CONSENT_TYPE = "consent_type"
        private const val KEY_NO_ADS = "no_ads"
        private const val KEY_UMP_CONSENT_STATUS = "ump_consent_status"
        
        const val CONSENT_PERSONALIZED = "personalized"
        const val CONSENT_NON_PERSONALIZED = "non_personalized"
        const val CONSENT_NO_ADS = "no_ads"
    }
    
    /**
     * Save consent type (personalized, non-personalized, no-ads)
     */
    fun saveConsentType(consentType: String) {
        prefs.edit().putString(KEY_CONSENT_TYPE, consentType).apply()
    }
    
    /**
     * Get current consent type
     */
    fun getConsentType(): String {
        return prefs.getString(KEY_CONSENT_TYPE, CONSENT_PERSONALIZED) ?: CONSENT_PERSONALIZED
    }
    
    /**
     * Check if user has opted for no ads
     */
    fun isNoAdsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NO_ADS, false)
    }
    
    /**
     * Set no ads preference
     */
    fun setNoAds(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NO_ADS, enabled).apply()
        if (enabled) {
            saveConsentType(CONSENT_NO_ADS)
        }
    }
    
    /**
     * Save UMP consent status
     */
    fun saveUmpConsentStatus(status: Int) {
        prefs.edit().putInt(KEY_UMP_CONSENT_STATUS, status).apply()
    }
    
    /**
     * Get UMP consent status
     */
    fun getUmpConsentStatus(): Int {
        return prefs.getInt(KEY_UMP_CONSENT_STATUS, 0)
    }
    
    /**
     * Clear all consent data (for reset)
     */
    fun clearConsent() {
        prefs.edit().clear().apply()
    }
}
