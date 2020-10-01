package nl.yussef.ali.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.yussef.ali.repository.SavedArticleRepository

class SavedArticleViewModelFactory(
    private val savedArticleRepository: SavedArticleRepository,
    val context: Context
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SavedArticleViewModel(savedArticleRepository, context) as T
    }
}