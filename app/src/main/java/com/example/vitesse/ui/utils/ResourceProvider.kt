package com.example.vitesse.ui.utils

import android.content.Context
import javax.inject.Inject

/**
 * A utility class for accessing application resources, such as strings.
 *
 * This class is designed to be injected using dependency injection, making resource
 * management easier and more testable in ViewModels or other classes.
 *
 * @param context The application context used to access resources.
 */
class ResourceProvider @Inject constructor(private val context: Context) {

    /**
     * Retrieves a string resource by its resource ID.
     *
     * @param resId The resource ID of the string to retrieve.
     * @return The string associated with the specified resource ID.
     */
    fun getString(resId: Int): String {
        return context.getString(resId)
    }
}
