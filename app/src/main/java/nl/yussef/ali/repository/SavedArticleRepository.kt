package nl.yussef.ali.repository

import nl.yussef.ali.api.RetrofitInstance

class SavedArticleRepository {

    suspend fun getLikedArticles(authToken: String) =
        RetrofitInstance.api.getLikedArticles(authToken)

    suspend fun likeAnArticle(id: Int, authToken: String) =
        RetrofitInstance.api.likeAnArticle(id, authToken)

    suspend fun unlikeAnArticle(id: Int, authToken: String) =
        RetrofitInstance.api.unlikeAnArticle(id, authToken)
}