package com.example.iqmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.iqmaster.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentSettingsScreen(
    consentStatus: String,
    noAdsEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onReopenConsentForm: () -> Unit,
    onNoAdsToggled: (Boolean) -> Unit,
    onNavigateToHostedPolicy: () -> Unit,
    onNavigateToLocalPolicy: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.consent_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Consent status
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Consent Information",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.consent_status_label, consentStatus),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Change ad preferences
            Button(
                onClick = onReopenConsentForm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reopen_consent_form))
            }
            
            // No Ads toggle
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.no_ads_toggle),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Disable all ads in the app",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = noAdsEnabled,
                        onCheckedChange = onNoAdsToggled
                    )
                }
            }
            
            Divider()
            
            // Privacy policy links
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedButton(
                onClick = onNavigateToHostedPolicy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_hosted_policy))
            }
            
            OutlinedButton(
                onClick = onNavigateToLocalPolicy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_local_policy))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info text
            Text(
                text = "About Consent",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "We use Google's User Messaging Platform (UMP) to manage ad consent. " +
                        "You can change your ad personalization preferences at any time. " +
                        "Enabling 'No Ads' will disable all advertisements in the app.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
