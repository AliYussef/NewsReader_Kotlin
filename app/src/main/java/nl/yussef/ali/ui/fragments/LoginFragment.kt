package nl.yussef.ali.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_login.*
import nl.yussef.ali.R
import nl.yussef.ali.model.User
import nl.yussef.ali.ui.activities.HomeActivity
import nl.yussef.ali.ui.viewModel.ArticleViewModel
import nl.yussef.ali.ui.viewModel.SavedArticleViewModel
import nl.yussef.ali.ui.viewModel.UserViewModel
import nl.yussef.ali.util.Resource
import nl.yussef.ali.util.SessionManager

class LoginFragment : Fragment(R.layout.fragment_login) {
    lateinit var viewModel: UserViewModel
    lateinit var savedArticleViewModel: SavedArticleViewModel
    lateinit var articleViewModel: ArticleViewModel
    lateinit var sessionManager: SessionManager
    lateinit var user: User

    companion object {
        const val TAG = "LoginFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as HomeActivity).userViewModel
        savedArticleViewModel = (activity as HomeActivity).savedArticleViewModel
        articleViewModel = (activity as HomeActivity).articleViewModel
        sessionManager = SessionManager(view.context)
        initializeUI()

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer { response ->
            loading.visibility = View.GONE
            when (response) {
                is Resource.Success -> {
                    password.onEditorAction(EditorInfo.IME_ACTION_DONE)
                    try {
                        response.data?.AuthToken?.let {
                            sessionManager.createSession(
                                user.username,
                                it
                            )
                        }
                        Toast.makeText(
                            requireContext(),
                            "${getString(R.string.welcome)} ${user.username}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        savedArticleViewModel.getLikedArticles(sessionManager.getUserDetails().AuthToken.toString())
                        articleViewModel.articles.value?.data?.Results?.clear()
                        findNavController().navigate(
                            R.id.action_loginFragment_to_articlesFragment
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "${getString(R.string.error_log)} ${e.message}")
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.wrong_credentials,
                        Toast.LENGTH_LONG
                    ).show()
                    response.message?.let { message ->
                        Log.e(TAG, "${getString(R.string.error_log)} $message")
                    }
                }
            }
        })

    }

    private fun initializeUI() {
        register.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        login.setOnClickListener {
            val username = username.text.toString().trim()
            val uPassword = password.text.toString().trim()
            if (isPasswordValid(uPassword)) {
                user = User(username, uPassword)
                loading.visibility = View.VISIBLE
                viewModel.login(user)
            } else {
                password.error = getString(R.string.password_length)
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}

