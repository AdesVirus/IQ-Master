package com.example.iqmaster.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.iqmaster.R
import com.example.iqmaster.data.local.ConsentStorage
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manager for interstitial ads
 */
class InterstitialAdManager(
    private val context: Context,
    private val consentStorage: ConsentStorage
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    companion object {
        private const val TAG = "InterstitialAdManager"
        private const val INTERSTITIAL_SHOW_PROBABILITY = 0.3f // 30% chance
    }
    
    /**
     * Load interstitial ad
     */
    fun loadAd() {
        if (consentStorage.isNoAdsEnabled()) {
            Log.d(TAG, "No ads enabled, skipping load")
            return
        }
        
        if (isLoading || interstitialAd != null) {
            Log.d(TAG, "Ad already loading or loaded")
            return
        }
        
        isLoading = true
        val adRequest = buildAdRequest()
        val adUnitId = context.getString(R.string.interstitial_ad_unit_id)
        
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isLoading = false
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }
    
    /**
     * Show interstitial ad if available and probability check passes
     */
    fun maybeShowAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (consentStorage.isNoAdsEnabled()) {
            Log.d(TAG, "No ads enabled, skipping show")
            onAdDismissed()
            return
        }
        
        // Random probability check
        if (Math.random() > INTERSTITIAL_SHOW_PROBABILITY) {
            Log.d(TAG, "Probability check failed, not showing ad")
            onAdDismissed()
            return
        }
        
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    interstitialAd = null
                    loadAd() // Preload next ad
                    onAdDismissed()
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    onAdDismissed()
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed")
                }
            }
            
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready")
            onAdDismissed()
            loadAd() // Try to load for next time
        }
    }
    
    /**
     * Build ad request with consent parameters
     */
    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        
        // Add npa=1 for non-personalized ads
        if (consentStorage.getConsentType() == ConsentStorage.CONSENT_NON_PERSONALIZED) {
            builder.addNetworkExtrasBundle(
                com.google.ads.mediation.admob.AdMobAdapter::class.java,
                android.os.Bundle().apply {
                    putString("npa", "1")
                }
            )
            Log.d(TAG, "Building ad request with npa=1 (non-personalized)")
        }
        
        return builder.build()
    }
}
