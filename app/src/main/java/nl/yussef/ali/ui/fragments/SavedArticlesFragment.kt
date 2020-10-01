package nl.yussef.ali.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_saved_articles.*
import kotlinx.android.synthetic.main.fragment_saved_articles.paginationProgressBar
import kotlinx.android.synthetic.main.fragment_saved_articles.recycleView
import nl.yussef.ali.R
import nl.yussef.ali.adapters.ArticleAdapter
import nl.yussef.ali.ui.activities.HomeActivity
import nl.yussef.ali.ui.viewModel.ArticleViewModel
import nl.yussef.ali.ui.viewModel.SavedArticleViewModel
import nl.yussef.ali.util.Resource
import nl.yussef.ali.util.SessionManager

class SavedArticlesFragment : Fragment(R.layout.fragment_saved_articles) {

    lateinit var viewModel: SavedArticleViewModel
    lateinit var articleViewModel: ArticleViewModel
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var sessionManager: SessionManager
    lateinit var authToken: String
    var isLoading = false

    companion object {
        const val TAG = "SavedArticlesFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as HomeActivity).savedArticleViewModel
        articleViewModel = (activity as HomeActivity).articleViewModel
        sessionManager = SessionManager(view.context)
        setupRecyclerView()

        swipeRefreshLayoutSaved.setOnRefreshListener {
            if (sessionManager.isLogin()) {
                viewModel.savedArticles.value?.data?.Results?.clear()
                viewModel.getLikedArticles(authToken)
            }
            swipeRefreshLayoutSaved.isRefreshing = false
        }

        if (sessionManager.isLogin()) {
            loginToSaveText.visibility = View.GONE
            faceIcon.visibility = View.GONE
            authToken = sessionManager.getUserDetails().AuthToken.toString()
            initializeUI()
            if (viewModel.savedArticles.value?.data?.Results.isNullOrEmpty()) {
                viewModel.getLikedArticles(authToken)
            }
        }
    }

    private fun initializeUI() {
        articleAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_savedArticlesFragment_to_detailsArticleActivity,
                bundle
            )
        }

        articleAdapter.setOnLikedButtonClickListener { article, i ->
            viewModel.unlikeAnArticle(article.Id!!, authToken!!)
            Toast.makeText(
                requireContext(),
                R.string.remove_from_favourite,
                Toast.LENGTH_SHORT
            ).show()
            viewModel.savedArticles.value?.data?.Results?.removeAt(i)
            articleAdapter.differ.submitList(viewModel.savedArticles.value?.data?.Results?.toList())
        }
        viewModel.savedArticles.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { articleResponse ->
                        articleAdapter.differ.submitList(articleResponse.Results.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "${getString(R.string.error_log)}: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter()
        recycleView.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }
}