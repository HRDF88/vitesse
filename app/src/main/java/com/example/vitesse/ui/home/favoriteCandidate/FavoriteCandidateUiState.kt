package com.example.vitesse.ui.home.favoriteCandidate

/**
 * Represents the UI state for the Favorite Candidate feature.
 * This class holds the current state of the favorite candidates, including any errors
 * that might occur during the process of marking candidates as favorites.
 *
 * @property error A string that contains the error message, if any, encountered while handling
 *                  favorite candidate operations. Defaults to an empty string if no error is present.
 */
data class FavoriteCandidateUiState (
    val error : String =""
)
