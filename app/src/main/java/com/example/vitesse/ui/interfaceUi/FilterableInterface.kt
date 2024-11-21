package com.example.vitesse.ui.interfaceUi

/**
 * Interface to be implemented by components that need to support filtering functionality.
 * This interface defines a method for filtering data based on a provided query string.
 *
 * Classes or fragments implementing this interface should define how to filter their data
 * collection (e.g., list, database results) based on the query provided.
 */
interface FilterableInterface {

    /**
     * Filters the data based on the provided query string.
     *
     * @param query The query string used to filter the data. It could be a name, keyword, or
     *              any string that helps in narrowing down the data.
     */
    fun filter(query: String)
}