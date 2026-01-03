package com.example.iqmaster

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.iqmaster.ads.InterstitialAdManager
import com.example.iqmaster.consent.UMPConsentManager
import com.example.iqmaster.data.local.ConsentStorage
import com.example.iqmaster.data.local.HighScoreStorage
import com.example.iqmaster.data.repository.HighScoreRepository
import com.example.iqmaster.ui.navigation.Screen
import com.example.iqmaster.ui.screens.*
import com.example.iqmaster.ui.theme.IQMasterTheme
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private lateinit var consentStorage: ConsentStorage
    private lateinit var highScoreStorage: HighScoreStorage
    private lateinit var highScoreRepository: HighScoreRepository
    private lateinit var umpConsentManager: UMPConsentManager
    private lateinit var interstitialAdManager: InterstitialAdManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        // Initialize storage and managers
        consentStorage = ConsentStorage(this)
        highScoreStorage = HighScoreStorage(this)
        highScoreRepository = HighScoreRepository(highScoreStorage, firestore, firebaseAuth)
        umpConsentManager = UMPConsentManager(this, consentStorage, firestore, firebaseAuth)
        interstitialAdManager = InterstitialAdManager(this, consentStorage)
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) { initializationStatus ->
            Log.d(TAG, "Mobile Ads initialized: $initializationStatus")
        }
        
        // Authenticate anonymously with Firebase
        authenticateAnonymously()
        
        // Request UMP consent
        lifecycleScope.launch {
            requestConsent()
        }
        
        // Preload interstitial ad
        interstitialAdManager.loadAd()
        
        setContent {
            IQMasterApp()
        }
    }
    
    private fun authenticateAnonymously() {
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously()
                .addOnSuccessListener { result ->
                    Log.d(TAG, "Anonymous authentication successful: ${result.user?.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Anonymous authentication failed", e)
                    Toast.makeText(
                        this,
                        getString(R.string.firebase_auth_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Log.d(TAG, "User already authenticated: ${firebaseAuth.currentUser?.uid}")
        }
    }
    
    private suspend fun requestConsent() {
        try {
            umpConsentManager.requestConsentInfoUpdate(this)
            
            // Show form if available
            if (umpConsentManager.getConsentStatus() == 
                com.google.android.ump.ConsentInformation.ConsentStatus.REQUIRED) {
                umpConsentManager.loadAndShowConsentFormIfRequired(this) {
                    Log.d(TAG, "Consent form dismissed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting consent", e)
        }
    }
    
    @Composable
    fun IQMasterApp() {
        IQMasterTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                var scores by remember { mutableStateOf<List<com.example.iqmaster.data.model.HighScore>>(emptyList()) }
                
                // Load scores
                LaunchedEffect(Unit) {
                    highScoreRepository.getAllScores().collect { loadedScores ->
                        scores = loadedScores
                    }
                }
                
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        QuizScreen(
                            onNavigateToHighScores = {
                                navController.navigate(Screen.HighScores.route)
                            },
                            onNavigateToPrivacyPolicy = {
                                navController.navigate(Screen.HostedPrivacyPolicy.route)
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.ConsentSettings.route)
                            },
                            onQuizComplete = { score ->
                                // Save score
                                lifecycleScope.launch {
                                    highScoreRepository.saveScore(score)
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.score_saved),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    
                                    // Reload scores
                                    scores = highScoreRepository.getAllScores().first()
                                    
                                    // Maybe show interstitial ad
                                    interstitialAdManager.maybeShowAd(this@MainActivity) {
                                        Log.d(TAG, "Interstitial ad dismissed")
                                    }
                                    
                                    // Navigate to high scores
                                    navController.navigate(Screen.HighScores.route)
                                }
                            },
                            consentStorage = consentStorage
                        )
                    }
                    
                    composable(Screen.HighScores.route) {
                        HighScoresScreen(
                            scores = scores,
                            onNavigateBack = { navController.popBackStack() },
                            onClearLocalScores = {
                                highScoreRepository.clearLocalScores()
                                lifecycleScope.launch {
                                    scores = highScoreRepository.getAllScores().first()
                                }
                            },
                            onClearRemoteScores = {
                                lifecycleScope.launch {
                                    highScoreRepository.clearRemoteScores()
                                    scores = highScoreRepository.getAllScores().first()
                                }
                            },
                            onResetAdsChoice = {
                                umpConsentManager.resetConsent()
                                lifecycleScope.launch {
                                    requestConsent()
                                }
                            }
                        )
                    }
                    
                    composable(Screen.HostedPrivacyPolicy.route) {
                        HostedPrivacyPolicyScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.LocalPrivacyPolicy.route) {
                        LocalPrivacyPolicyScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.ConsentSettings.route) {
                        ConsentSettingsScreen(
                            consentStatus = umpConsentManager.getConsentStatusString(),
                            noAdsEnabled = consentStorage.isNoAdsEnabled(),
                            onNavigateBack = { navController.popBackStack() },
                            onReopenConsentForm = {
                                umpConsentManager.resetConsent()
                                lifecycleScope.launch {
                                    requestConsent()
                                }
                            },
                            onNoAdsToggled = { enabled ->
                                consentStorage.setNoAds(enabled)
                            },
                            onNavigateToHostedPolicy = {
                                navController.navigate(Screen.HostedPrivacyPolicy.route)
                            },
                            onNavigateToLocalPolicy = {
                                navController.navigate(Screen.LocalPrivacyPolicy.route)
                            }
                        )
                    }
                }
            }
        }
    }
}
