package com.example.iqmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.iqmaster.R
import com.example.iqmaster.ads.BannerAdHelper
import com.example.iqmaster.data.local.ConsentStorage
import com.example.iqmaster.data.model.QuizQuestions
import com.google.android.gms.ads.AdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateToHighScores: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onQuizComplete: (Int) -> Unit,
    consentStorage: ConsentStorage
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    
    val questions = remember { QuizQuestions.questions }
    val currentQuestion = questions[currentQuestionIndex]
    val context = LocalContext.current
    
    // Create banner ad
    val bannerAd = remember { BannerAdHelper.createBannerAd(context, consentStorage) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_screen_title)) },
                actions = {
                    TextButton(onClick = onNavigateToSettings) {
                        Text("Settings")
                    }
                }
            )
        },
        bottomBar = {
            // Banner ad at bottom
            if (bannerAd != null) {
                AndroidView(
                    factory = { bannerAd },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score display
            Text(
                text = stringResource(R.string.score_label, score),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Question number
            Text(
                text = stringResource(
                    R.string.question_label,
                    currentQuestionIndex + 1,
                    questions.size
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Question text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = currentQuestion.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            // Answer options
            currentQuestion.options.forEachIndexed { index, option ->
                val isCorrect = index == currentQuestion.correctAnswerIndex
                val isSelected = selectedOptionIndex == index
                val colors = when {
                    isAnswered && isCorrect -> CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    isAnswered && isSelected && !isCorrect -> CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                    else -> CardDefaults.cardColors()
                }
                
                Card(
                    onClick = {
                        if (!isAnswered) {
                            selectedOptionIndex = index
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = colors
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            if (!isAnswered) {
                Button(
                    onClick = {
                        if (selectedOptionIndex != null) {
                            isAnswered = true
                            if (selectedOptionIndex == currentQuestion.correctAnswerIndex) {
                                score += 10
                            }
                        }
                    },
                    enabled = selectedOptionIndex != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.submit_answer_button))
                }
            } else {
                if (currentQuestionIndex < questions.size - 1) {
                    Button(
                        onClick = {
                            currentQuestionIndex++
                            selectedOptionIndex = null
                            isAnswered = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.next_question_button))
                    }
                } else {
                    Button(
                        onClick = { showCompletionDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.finish_test_button))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Navigation buttons
            OutlinedButton(
                onClick = onNavigateToHighScores,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View High Scores")
            }
            
            OutlinedButton(
                onClick = onNavigateToPrivacyPolicy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Privacy Policy")
            }
        }
    }
    
    // Completion dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Quiz Complete!") },
            text = { Text("Your final score: $score out of ${questions.size * 10}") },
            confirmButton = {
                Button(
                    onClick = {
                        showCompletionDialog = false
                        onQuizComplete(score)
                    }
                ) {
                    Text("Save Score")
                }
            }
        )
    }
}
