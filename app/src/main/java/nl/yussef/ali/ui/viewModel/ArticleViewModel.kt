package nl.yussef.ali.ui.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.yussef.ali.R
import nl.yussef.ali.model.Article
import nl.yussef.ali.model.Result
import nl.yussef.ali.repository.ArticleRepository
import nl.yussef.ali.util.Resource
import retrofit2.Response

class ArticleViewModel(private val articleRepository: ArticleRepository, val context: Context) :
    ViewModel() {

    private val _articles: MutableLiveData<Resource<Result>> = MutableLiveData()
    val articles: MutableLiveData<Resource<Result>>
        get() = _articles
    private var filteringFees: MutableList<String> = ArrayList()
    var filteredArticles: MutableList<Article?> = ArrayList()
    private var articleResponse: Result? = null
    private var nextId: Int? = null

    companion object {
        const val TAG = "ArticleViewModel"
    }

    fun getArticles(authToken: String?) = viewModelScope.launch {
        try {
            _articles.postValue(Resource.Loading())
            val response = articleRepository.getArticles(authToken)
            _articles.postValue(handleArticlesResponse(response))
        } catch (e: Exception) {
            _articles.postValue(Resource.Error(context.getString(R.string.connection_error)))
            Log.e(TAG, "${context.getString(R.string.error_log)} ${e.message}")
        }
    }

    fun getNextArticles(authToken: String?) = viewModelScope.launch {
        try {
            _articles.postValue(Resource.Loading())
            val response = nextId?.let { articleRepository.getNextArticles(it, 20, authToken) }
            _articles.postValue(response?.let { handleArticlesResponse(it) })
        } catch (e: Exception) {
            _articles.postValue(Resource.Error(context.getString(R.string.connection_error)))
            Log.e(TAG, "${context.getString(R.string.error_log)} ${e.message}")
        }
    }

    fun getArticlesByCategory(authToken: String?, category: String) = viewModelScope.launch {
        try {
            _articles.postValue(Resource.Loading())
            val response = articleRepository.getArticles(authToken)
            _articles.postValue(handleArticlesByCategoryResponse(response, category))
        } catch (e: Exception) {
            _articles.postValue(Resource.Error(context.getString(R.string.connection_error)))
            Log.e(TAG, "${context.getString(R.string.error_log)} ${e.message}")
        }
    }

    fun getNextArticlesByCategory(authToken: String?, category: String) = viewModelScope.launch {
        try {
            _articles.postValue(Resource.Loading())
            val response = nextId?.let { articleRepository.getNextArticles(it, 20, authToken) }
            _articles.postValue(response?.let { handleArticlesByCategoryResponse(it, category) })
        } catch (e: Exception) {
            _articles.postValue(Resource.Error(context.getString(R.string.connection_error)))
            Log.e(TAG, "${context.getString(R.string.error_log)} ${e.message}")
        }
    }

    fun filterArticles(feed: String?) {
        feed?.let { filteringFees.add(feed) }
        val result =
            _articles.value?.data?.Results?.filter { filteringFees.contains(it.Categories?.get(0)?.Name) }
        result?.let { filteredArticles = it as MutableList<Article?> }
    }

    fun removeFilter(feed: String) {
        val temp = filteredArticles.filter { it?.Categories?.get(0)?.Name == feed }

        temp.forEach {
            if (filteredArticles.contains(it)) filteredArticles.remove(it)
        }
        filteringFees.remove(feed)
    }

    private fun handleArticlesResponse(response: Response<Result>): Resource<Result> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (articleResponse == null) {
                    articleResponse = resultResponse
                    nextId = resultResponse.NextId
                } else {
                    val oldArticles = articleResponse?.Results
                    val newArticles = resultResponse.Results
                    oldArticles?.addAll(newArticles)
                    nextId = resultResponse.NextId
                }
                return Resource.Success(articleResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleArticlesByCategoryResponse(
        response: Response<Result>,
        category: String
    ): Resource<Result> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (articleResponse == null) {
                    articleResponse = filterArticlesByCategory(resultResponse, category)
                    nextId = resultResponse.NextId
                } else {
                    val result = filterArticlesByCategory(resultResponse, category)
                    val oldArticles = articleResponse?.Results
                    val newArticles = result.Results
                    oldArticles?.addAll(newArticles)
                    nextId = resultResponse.NextId
                }
                return Resource.Success(articleResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun filterArticlesByCategory(resultResponse: Result, category: String): Result {
        val result =
            resultResponse.Results.filter {
                it.Categories?.let { categories -> categories.any { cat -> cat.Name == category } } as Boolean
            }
        return Result(result as MutableList<Article>, resultResponse.NextId)
    }
}