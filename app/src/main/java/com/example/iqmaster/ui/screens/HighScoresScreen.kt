package com.example.iqmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.iqmaster.R
import com.example.iqmaster.data.model.HighScore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighScoresScreen(
    scores: List<HighScore>,
    onNavigateBack: () -> Unit,
    onClearLocalScores: () -> Unit,
    onClearRemoteScores: () -> Unit,
    onResetAdsChoice: () -> Unit
) {
    var showClearLocalDialog by remember { mutableStateOf(false) }
    var showClearRemoteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.high_scores_title)) },
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
        ) {
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showClearLocalDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear Local", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { showClearRemoteDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear Cloud", maxLines = 1)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onResetAdsChoice,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reset_ads_choice))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scores list
            if (scores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_scores_yet),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(
                    text = "Top Scores",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scores) { score ->
                        ScoreItem(score = score)
                    }
                }
            }
        }
    }
    
    // Confirmation dialogs
    if (showClearLocalDialog) {
        AlertDialog(
            onDismissRequest = { showClearLocalDialog = false },
            title = { Text("Clear Local Scores") },
            text = { Text(stringResource(R.string.confirm_clear_local)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLocalScores()
                        showClearLocalDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLocalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showClearRemoteDialog) {
        AlertDialog(
            onDismissRequest = { showClearRemoteDialog = false },
            title = { Text("Clear Cloud Scores") },
            text = { Text(stringResource(R.string.confirm_clear_remote)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearRemoteScores()
                        showClearRemoteDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearRemoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ScoreItem(score: HighScore) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val dateString = remember(score) {
        dateFormat.format(Date(score.localTimestamp))
    }
    
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
            Column {
                Text(
                    text = "Score: ${score.score}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
