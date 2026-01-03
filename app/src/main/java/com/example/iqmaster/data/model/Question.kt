package com.example.iqmaster.data.model

/**
 * Quiz question model
 */
data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

/**
 * Sample IQ quiz questions
 */
object QuizQuestions {
    val questions = listOf(
        Question(
            id = 1,
            text = "What number comes next in this sequence: 2, 4, 8, 16, ?",
            options = listOf("24", "32", "20", "28"),
            correctAnswerIndex = 1
        ),
        Question(
            id = 2,
            text = "If all Bloops are Razzies and all Razzies are Lazzies, then all Bloops are definitely Lazzies?",
            options = listOf("True", "False", "Cannot determine", "Sometimes"),
            correctAnswerIndex = 0
        ),
        Question(
            id = 3,
            text = "Which word does NOT belong: Apple, Orange, Carrot, Banana",
            options = listOf("Apple", "Orange", "Carrot", "Banana"),
            correctAnswerIndex = 2
        ),
        Question(
            id = 4,
            text = "What is 15% of 200?",
            options = listOf("25", "30", "35", "40"),
            correctAnswerIndex = 1
        ),
        Question(
            id = 5,
            text = "Complete the analogy: Book is to Reading as Fork is to ?",
            options = listOf("Drawing", "Writing", "Eating", "Stirring"),
            correctAnswerIndex = 2
        ),
        Question(
            id = 6,
            text = "Which number is the odd one out: 2, 3, 5, 7, 9, 11?",
            options = listOf("2", "3", "9", "11"),
            correctAnswerIndex = 2
        ),
        Question(
            id = 7,
            text = "If you rearrange the letters 'CIFAIPC', you would have the name of a(n):",
            options = listOf("City", "Animal", "Ocean", "Country"),
            correctAnswerIndex = 2
        ),
        Question(
            id = 8,
            text = "What is the next letter in this sequence: A, C, F, J, ?",
            options = listOf("M", "N", "O", "P"),
            correctAnswerIndex = 2
        ),
        Question(
            id = 9,
            text = "If 5 cats can catch 5 mice in 5 minutes, how many cats are needed to catch 100 mice in 100 minutes?",
            options = listOf("5", "10", "20", "100"),
            correctAnswerIndex = 0
        ),
        Question(
            id = 10,
            text = "What comes next: 1, 1, 2, 3, 5, 8, ?",
            options = listOf("11", "12", "13", "14"),
            correctAnswerIndex = 2
        )
    )
}
