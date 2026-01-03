package com.example.iqmaster.ads

import android.content.Context
import android.util.Log
import com.example.iqmaster.R
import com.example.iqmaster.data.local.ConsentStorage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Helper for creating banner ads
 */
object BannerAdHelper {
    private const val TAG = "BannerAdHelper"
    
    /**
     * Create and configure a banner ad view
     */
    fun createBannerAd(context: Context, consentStorage: ConsentStorage): AdView? {
        if (consentStorage.isNoAdsEnabled()) {
            Log.d(TAG, "No ads enabled, not creating banner")
            return null
        }
        
        val adView = AdView(context)
        adView.adUnitId = context.getString(R.string.banner_ad_unit_id)
        adView.setAdSize(AdSize.BANNER)
        
        val adRequest = buildAdRequest(consentStorage)
        adView.loadAd(adRequest)
        
        return adView
    }
    
    /**
     * Build ad request with consent parameters
     */
    private fun buildAdRequest(consentStorage: ConsentStorage): AdRequest {
        val builder = AdRequest.Builder()
        
        // Add npa=1 for non-personalized ads
        if (consentStorage.getConsentType() == ConsentStorage.CONSENT_NON_PERSONALIZED) {
            builder.addNetworkExtrasBundle(
                com.google.ads.mediation.admob.AdMobAdapter::class.java,
                android.os.Bundle().apply {
                    putString("npa", "1")
                }
            )
            Log.d(TAG, "Building banner ad request with npa=1 (non-personalized)")
        }
        
        return builder.build()
    }
}
