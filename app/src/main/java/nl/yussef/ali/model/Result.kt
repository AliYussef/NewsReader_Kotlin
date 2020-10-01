package nl.yussef.ali.model

import java.io.Serializable

data class Result(
    val Results: MutableList<Article>,
    val NextId: Int
)

data class Article(
    val Id: Int?,
    val Title: String?,
    val Summary: String?,
    val PublishDate: String?,
    val Image: String?,
    val Url: String?,
    val Related: List<String>?,
    val Categories: List<Category>?,
    var IsLiked: Boolean?
) : Serializable

data class Category(
    val Id: Int?,
    val Name: String?,
) : Serializable