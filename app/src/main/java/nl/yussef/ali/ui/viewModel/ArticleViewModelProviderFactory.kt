package nl.yussef.ali.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.yussef.ali.repository.ArticleRepository

class ArticleViewModelProviderFactory(
    private val articleRepository: ArticleRepository,
    val context: Context
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ArticleViewModel(articleRepository, context) as T
    }
}