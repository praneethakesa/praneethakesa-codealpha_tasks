package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Quote
import com.example.data.QuoteProvider
import com.example.data.QuoteRepository
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(application)

    private val _allQuotes = MutableStateFlow<List<Quote>>(emptyList())
    val allQuotes: StateFlow<List<Quote>> = _allQuotes.asStateFlow()
    
    private val _currentQuote = MutableStateFlow<Quote?>(null)
    val currentQuote: StateFlow<Quote?> = _currentQuote.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _networkError = MutableStateFlow<String?>(null)
    val networkError: StateFlow<String?> = _networkError.asStateFlow()

    // History stack to go backward
    private val history = mutableListOf<Quote>()
    private val _historyIndex = MutableStateFlow(-1)
    val canGoBack: StateFlow<Boolean> = MutableStateFlow(false) // dynamically calculated

    init {
        loadQuotes()
        showRandomQuote()
    }

    private fun loadQuotes() {
        val initial = QuoteProvider.initialQuotes
        val custom = repository.getCustomQuotes()
        _allQuotes.value = initial + custom
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        // Reset history when switching categories to ensure fresh navigation
        history.clear()
        _historyIndex.value = -1
        showRandomQuote()
    }

    fun showRandomQuote() {
        val category = _selectedCategory.value
        
        // If category is "Favorites" or "My Quotes", fetch locally
        if (category == "Favorites" || category == "My Quotes") {
            showLocalRandomQuote()
            return
        }

        // Fetch from the API
        viewModelScope.launch {
            _isLoading.value = true
            _networkError.value = null
            try {
                // Map local category selection to Quotable tags
                val apiTag = when (category) {
                    "Wisdom" -> "wisdom"
                    "Motivation" -> "motivational"
                    "Science" -> "science"
                    "Art" -> "art"
                    else -> null // "All" category
                }

                val remoteQuote = RetrofitClient.quotableApi.getRandomQuote(apiTag)
                val newQuote = Quote(
                    id = "remote_" + (remoteQuote.id ?: System.currentTimeMillis().toString()),
                    text = remoteQuote.content,
                    author = remoteQuote.author,
                    category = category
                )
                
                pushToHistoryAndSet(newQuote)
            } catch (e: Exception) {
                // Try fallback to ZenQuotes API for more robustness
                try {
                    val fallbackQuotes = RetrofitClient.zenQuotesApi.getRandomQuote()
                    if (fallbackQuotes.isNotEmpty()) {
                        val zen = fallbackQuotes[0]
                        val newQuote = Quote(
                            id = "zen_" + System.currentTimeMillis(),
                            text = zen.q,
                            author = zen.a,
                            category = category
                        )
                        pushToHistoryAndSet(newQuote)
                    } else {
                        throw Exception("Empty fallback api response")
                    }
                } catch (fallbackEx: Exception) {
                    // Gracefully fallback to local offline pre-populated quotes
                    _networkError.value = "Offline Mode active"
                    showLocalRandomQuote()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun showLocalRandomQuote() {
        val filtered = getFilteredQuotes()
        if (filtered.isEmpty()) {
            _currentQuote.value = null
            _isFavorite.value = false
            return
        }

        // Get a random quote that is different from the current one if possible
        var nextQuote = filtered[Random.nextInt(filtered.size)]
        if (filtered.size > 1 && nextQuote.id == _currentQuote.value?.id) {
            val otherQuotes = filtered.filter { it.id != _currentQuote.value?.id }
            nextQuote = otherQuotes[Random.nextInt(otherQuotes.size)]
        }

        pushToHistoryAndSet(nextQuote)
    }

    private fun pushToHistory(quote: Quote) {
        if (_historyIndex.value < history.size - 1) {
            // If we went back and then requested a new one, slice the history up to our current index
            val updatedHistory = history.subList(0, _historyIndex.value + 1).toMutableList()
            updatedHistory.add(quote)
            history.clear()
            history.addAll(updatedHistory)
        } else {
            history.add(quote)
        }
        _historyIndex.value = history.size - 1
    }

    private fun pushToHistoryAndSet(quote: Quote) {
        pushToHistory(quote)
        _currentQuote.value = quote
        updateFavoriteStatus()
    }

    fun goBack() {
        if (_historyIndex.value > 0) {
            _historyIndex.value -= 1
            val prevQuote = history[_historyIndex.value]
            _currentQuote.value = prevQuote
            updateFavoriteStatus()
        }
    }

    fun toggleFavoriteCurrent() {
        val current = _currentQuote.value ?: return
        val newFavStatus = repository.toggleFavorite(current)
        _isFavorite.value = newFavStatus
        
        // If the filter is "Favorites", and we unfavorited the current quote, show another random one
        if (_selectedCategory.value == "Favorites" && !newFavStatus) {
            showRandomQuote()
        }
    }

    fun addCustomQuote(text: String, author: String, category: String) {
        val newQuote = repository.addCustomQuote(text, author, category)
        loadQuotes()
        
        // Set the newly created quote as the active quote for instant gratification!
        _currentQuote.value = newQuote
        // Add to history
        history.add(newQuote)
        _historyIndex.value = history.size - 1
        updateFavoriteStatus()
    }

    fun deleteCurrentCustomQuote() {
        val current = _currentQuote.value ?: return
        if (current.isCustom) {
            repository.deleteCustomQuote(current.id)
            loadQuotes()
            // Reset history and load a new random quote
            history.clear()
            _historyIndex.value = -1
            showRandomQuote()
        }
    }

    fun updateFavoriteStatus() {
        val current = _currentQuote.value
        _isFavorite.value = if (current != null) repository.isFavorite(current.id) else false
    }

    fun hasPrevious(): Boolean {
        return _historyIndex.value > 0
    }

    private fun getFilteredQuotes(): List<Quote> {
        val quotes = _allQuotes.value
        return when (val cat = _selectedCategory.value) {
            "All" -> quotes
            "Favorites" -> {
                val favIds = repository.getFavorites()
                val savedFavorites = repository.getSavedFavoritedQuotes()
                val combined = quotes + savedFavorites
                combined.filter { favIds.contains(it.id) }.distinctBy { it.id }
            }
            "My Quotes" -> quotes.filter { it.isCustom }
            else -> quotes.filter { it.category == cat }
        }
    }
}
