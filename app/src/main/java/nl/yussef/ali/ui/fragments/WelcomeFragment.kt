package nl.yussef.ali.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome.*
import nl.yussef.ali.R
import nl.yussef.ali.ui.activities.HomeActivity
import nl.yussef.ali.ui.viewModel.ArticleViewModel
import nl.yussef.ali.ui.viewModel.SavedArticleViewModel
import nl.yussef.ali.util.SessionManager

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {
    private lateinit var sessionManager: SessionManager
    lateinit var articleViewModel: ArticleViewModel
    lateinit var savedArticleViewModel: SavedArticleViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        articleViewModel = (activity as HomeActivity).articleViewModel
        savedArticleViewModel = (activity as HomeActivity).savedArticleViewModel
        sessionManager = SessionManager(view.context)
        showGreeting()
        initializeUI()

    }

    private fun initializeUI() {
        logout.setOnClickListener {
            sessionManager.clearUserDetails()
            articleViewModel.articles.value?.data?.Results?.clear()

            findNavController().navigate(
                R.id.action_welcomeFragment_to_articlesFragment
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showGreeting() {
        val username = sessionManager.getUserDetails().username.toString()
        welcomeText.text = "${getString(R.string.welcome)} $username"
    }
}