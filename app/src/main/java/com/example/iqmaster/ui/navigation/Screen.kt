package com.example.iqmaster.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object HighScores : Screen("high_scores")
    object HostedPrivacyPolicy : Screen("privacy_policy_hosted")
    object LocalPrivacyPolicy : Screen("privacy_policy_local")
    object ConsentSettings : Screen("consent_settings")
}
