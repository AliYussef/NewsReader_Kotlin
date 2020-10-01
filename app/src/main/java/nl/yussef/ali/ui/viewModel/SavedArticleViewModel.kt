package nl.yussef.ali.ui.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.yussef.ali.R
import nl.yussef.ali.model.Result
import nl.yussef.ali.repository.SavedArticleRepository
import nl.yussef.ali.util.Resource
import nl.yussef.ali.util.SessionManager
import retrofit2.Response

class SavedArticleViewModel(
    private val savedArticleRepository: SavedArticleRepository,
    val context: Context
) :
    ViewModel() {

    companion object {
        const val TAG = "SavedArticleViewModel"
    }

    private val _savedArticles: MutableLiveData<Resource<Result>> = MutableLiveData()
    val savedArticles: LiveData<Resource<Result>>
        get() = _savedArticles

    private val _actionOnSavedArticles: MutableLiveData<Resource<Unit>> = MutableLiveData()
    val actionOnSavedArticles: LiveData<Resource<Unit>>
        get() = _actionOnSavedArticles

    fun getLikedArticles(authToken: String) = viewModelScope.launch {
        try {
            _savedArticles.value = Resource.Loading()
            _savedArticles.value =
                handleSavedArticleResponse(savedArticleRepository.getLikedArticles(authToken))
        } catch (e: Exception) {
            _savedArticles.postValue(Resource.Error(context.getString(R.string.connection_error)))
            Log.e(TAG, "${context.getString(R.string.error_log)} ${e.message}")
        }
    }

    fun likeAnArticle(id: Int, authToken: String) = viewModelScope.launch {
        try {
            _actionOnSavedArticles.value =
                handleActionOnSavedArticle(savedArticleRepository.likeAnArticle(id, authToken))
        } catch (e: Exception) {
            Log.e(TAG, "${context.getString(R.string.error_log)} ${e.message}")
        }
    }

    fun unlikeAnArticle(id: Int, authToken: String) = viewModelScope.launch {
        try {
            _actionOnSavedArticles.value =
                handleActionOnSavedArticle(savedArticleRepository.unlikeAnArticle(id, authToken))
        } catch (e: Exception) {
            Log.e(
                TAG,
                "${context.getString(R.string.error_log)} ${e.message}"
            )
        }
    }

    private fun handleSavedArticleResponse(response: Response<Result>): Resource<Result> {
        if (response.isSuccessful && response.body() != null) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleActionOnSavedArticle(response: Response<Unit>): Resource<Unit> {
        if (response.isSuccessful && response.body() != null) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }
}