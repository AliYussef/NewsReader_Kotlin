package nl.yussef.ali.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.yussef.ali.repository.UserRepository

class UserViewModelProviderFactory(
    private val userRepository: UserRepository,
    private val context: Context
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserViewModel(userRepository, context) as T
    }

}