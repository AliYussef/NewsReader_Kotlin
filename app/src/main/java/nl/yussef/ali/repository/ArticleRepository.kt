package nl.yussef.ali.repository

import nl.yussef.ali.api.RetrofitInstance

class ArticleRepository {

    suspend fun getArticles(authToken: String?) = RetrofitInstance.api.getArticles(authToken)

    suspend fun getNextArticles(id: Int, count: Int, authToken: String?) =
        RetrofitInstance.api.getNextArticles(id, count, authToken)
}