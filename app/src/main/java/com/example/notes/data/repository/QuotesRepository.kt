package com.example.notes.data.repository

import com.example.notes.data.local.dao.QuoteDao
import com.example.notes.data.local.entities.Quote
import kotlinx.coroutines.flow.Flow

class QuotesRepository(private val quoteDao: QuoteDao) {
    val allQuotes: Flow<List<Quote>> = quoteDao.getAllQuotes()

    fun searchQuotes(query: String): Flow<List<Quote>> = quoteDao.searchQuotes(query)

    suspend fun getQuoteById(id: Long): Quote? = quoteDao.getQuoteById(id)

    suspend fun insertQuote(quote: Quote) {
        quoteDao.insertQuote(quote)
    }

    suspend fun addQuote(text: String, author: String, createdAt: Long = System.currentTimeMillis()) {
        val quote = Quote(text = text, author = author, createdAt = createdAt)
        quoteDao.insertQuote(quote)
    }

    suspend fun updateQuote(quote: Quote) {
        quoteDao.updateQuote(quote)
    }

    suspend fun deleteQuote(quote: Quote) {
        quoteDao.deleteQuote(quote)
    }

    suspend fun deleteAllQuotes() {
        quoteDao.deleteAllQuotes()
    }
}
