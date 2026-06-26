package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class QuoteRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("quote_generator_prefs", Context.MODE_PRIVATE)
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        
    private val quoteListAdapter = moshi.adapter<List<Quote>>(
        Types.newParameterizedType(List::class.java, Quote::class.java)
    )

    // Favorites
    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
    }

    fun getSavedFavoritedQuotes(): List<Quote> {
        val json = prefs.getString("saved_favorited_quotes_json", null) ?: return emptyList()
        return try {
            quoteListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavoritedQuote(quote: Quote) {
        val quotes = getSavedFavoritedQuotes().toMutableList()
        if (quotes.none { it.id == quote.id }) {
            quotes.add(quote)
            prefs.edit().putString("saved_favorited_quotes_json", quoteListAdapter.toJson(quotes)).apply()
        }
    }

    private fun removeSavedFavoritedQuote(quoteId: String) {
        val quotes = getSavedFavoritedQuotes().toMutableList()
        val index = quotes.indexOfFirst { it.id == quoteId }
        if (index != -1) {
            quotes.removeAt(index)
            prefs.edit().putString("saved_favorited_quotes_json", quoteListAdapter.toJson(quotes)).apply()
        }
    }

    fun toggleFavorite(quote: Quote): Boolean {
        val favorites = getFavorites().toMutableSet()
        val quoteId = quote.id
        val isFav = if (favorites.contains(quoteId)) {
            favorites.remove(quoteId)
            if (quoteId.startsWith("remote_") || quoteId.startsWith("zen_")) {
                removeSavedFavoritedQuote(quoteId)
            }
            false
        } else {
            favorites.add(quoteId)
            if (quoteId.startsWith("remote_") || quoteId.startsWith("zen_")) {
                saveFavoritedQuote(quote)
            }
            true
        }
        prefs.edit().putStringSet("favorite_ids", favorites).apply()
        return isFav
    }

    fun isFavorite(quoteId: String): Boolean {
        return getFavorites().contains(quoteId)
    }

    // Custom Quotes
    fun getCustomQuotes(): List<Quote> {
        val json = prefs.getString("custom_quotes_json", null) ?: return emptyList()
        return try {
            quoteListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addCustomQuote(text: String, author: String, category: String): Quote {
        val customQuotes = getCustomQuotes().toMutableList()
        val newQuote = Quote(
            id = "custom_" + System.currentTimeMillis(),
            text = text,
            author = if (author.trim().isEmpty()) "Unknown" else author,
            category = category,
            isCustom = true
        )
        customQuotes.add(newQuote)
        prefs.edit().putString("custom_quotes_json", quoteListAdapter.toJson(customQuotes)).apply()
        return newQuote
    }

    fun deleteCustomQuote(quoteId: String) {
        val customQuotes = getCustomQuotes().toMutableList()
        val index = customQuotes.indexOfFirst { it.id == quoteId }
        if (index != -1) {
            customQuotes.removeAt(index)
            prefs.edit().putString("custom_quotes_json", quoteListAdapter.toJson(customQuotes)).apply()
            
            // Also remove from favorites if it was there
            val favorites = getFavorites().toMutableSet()
            if (favorites.contains(quoteId)) {
                favorites.remove(quoteId)
                prefs.edit().putStringSet("favorite_ids", favorites).apply()
            }
        }
    }
}
