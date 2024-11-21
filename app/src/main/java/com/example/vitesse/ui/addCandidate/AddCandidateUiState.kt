package com.example.vitesse.ui.addCandidate

/**
 * Represents the UI state for adding a candidate in the application.
 *
 * This class is used to manage and observe the state of the UI during the candidate addition process,
 * including error handling, operation status, and loading state.
 *
 * @property error A string that contains an error message if an issue occurs during the addition process.
 * Default value is an empty string.
 * @property addResult A boolean indicating the result of the add operation.
 * `true` means the operation was successful, and `false` means it failed. Default value is `true`.
 * @property isLoading A boolean representing whether the addition process is currently in progress.
 * `true` indicates that the operation is loading, and `false` indicates that it is not. Default value is `false`.
 */
data class AddCandidateUiState(
    val error: String = "",
    val addResult: Boolean = true,
    val isLoading: Boolean = false
)
