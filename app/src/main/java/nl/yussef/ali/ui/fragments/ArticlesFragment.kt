package nl.yussef.ali.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.fragment_articles.paginationProgressBar
import kotlinx.android.synthetic.main.fragment_articles.recycleView
import kotlinx.android.synthetic.main.fragment_articles.swipeRefreshLayout
import nl.yussef.ali.R
import nl.yussef.ali.adapters.ArticleAdapter
import nl.yussef.ali.ui.viewModel.ArticleViewModel
import nl.yussef.ali.ui.activities.HomeActivity
import nl.yussef.ali.ui.viewModel.SavedArticleViewModel
import nl.yussef.ali.util.LinearLayoutManagerWrapper
import nl.yussef.ali.util.Resource
import nl.yussef.ali.util.SessionManager

class ArticlesFragment : Fragment(R.layout.fragment_articles) {
    lateinit var articleViewModel: ArticleViewModel
    lateinit var savedArticleViewModel: SavedArticleViewModel
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var sessionManager: SessionManager
    private var feedFilterList: MutableList<String> = ArrayList()
    private var authToken: String? = null
    var isLoading = false
    var isScrolling = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        articleViewModel = (activity as HomeActivity).articleViewModel
        savedArticleViewModel = (activity as HomeActivity).savedArticleViewModel
        sessionManager = SessionManager(view.context)
        setupRecyclerView()
        initializeUI()

        articleViewModel.articles.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    if (feedFilterList.isEmpty()) {
                        response.data?.let { articleResponse ->
                            articleAdapter.differ.submitList(articleResponse.Results.toList())
                        }
                    } else {
                        articleViewModel.filterArticles(null)
                        articleAdapter.differ.submitList(articleViewModel.filteredArticles.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Snackbar.make(
                        recycleView,
                        response.message!!,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(getString(R.string.retry)) {
                        articleViewModel.getArticles(authToken)
                    }.show()
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun initializeUI() {
        swipeRefreshLayout.setOnRefreshListener {
            articleViewModel.articles.value?.data?.Results?.clear()
            articleViewModel.getArticles(authToken)
            swipeRefreshLayout.isRefreshing = false
        }

        if (sessionManager.isLogin()) {
            authToken = sessionManager.getUserDetails().AuthToken.toString()
        }

        if (articleViewModel.articles.value?.data?.Results.isNullOrEmpty()) {
            articleViewModel.getArticles(authToken)
        }
        articleAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_articlesFragment_to_detailsArticleActivity,
                bundle
            )
        }

        articleAdapter.setOnLikedButtonClickListener { article, i ->
            if (!article.IsLiked!!) {
                savedArticleViewModel.likeAnArticle(article.Id!!, authToken!!)
                Toast.makeText(
                    requireContext(),
                    R.string.add_to_favourite,
                    Toast.LENGTH_SHORT
                ).show()
                articleAdapter.differ.currentList[i].IsLiked = true
                articleAdapter.notifyItemChanged(i)
                savedArticleViewModel.getLikedArticles(authToken!!)
            } else {
                savedArticleViewModel.unlikeAnArticle(article.Id!!, authToken!!)
                Toast.makeText(
                    requireContext(),
                    R.string.remove_from_favourite,
                    Toast.LENGTH_SHORT
                ).show()
                articleAdapter.differ.currentList[i].IsLiked = false
                articleAdapter.notifyItemChanged(i)
                savedArticleViewModel.getLikedArticles(authToken!!)
            }
        }

        generalFilter.setOnCheckedChangeListener(checkedChangeListener())
        sportFilter.setOnCheckedChangeListener(checkedChangeListener())
        gamesFilter.setOnCheckedChangeListener(checkedChangeListener())
        remarkableFilter.setOnCheckedChangeListener(checkedChangeListener())
        scienceFilter.setOnCheckedChangeListener(checkedChangeListener())
        internetFilter.setOnCheckedChangeListener(checkedChangeListener())
    }

    private fun checkedChangeListener() =
        CompoundButton.OnCheckedChangeListener { category, isChecked ->
            val feed = category.text.toString()
            if (isChecked) {
                feedFilterList.add(feed)
                articleViewModel.filterArticles(feed)
                articleAdapter.differ.submitList(articleViewModel.filteredArticles.toList())
            } else {
                feedFilterList.remove(feed)
                articleViewModel.removeFilter(feed)
                articleAdapter.differ.submitList(articleViewModel.filteredArticles.toList())
                if (feedFilterList.isEmpty()) {
                    articleAdapter.differ.submitList(articleViewModel.articles.value?.data?.Results?.toList())
                }
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

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter()
        recycleView.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManagerWrapper(activity)
            addOnScrollListener(this@ArticlesFragment.scrollListener)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val totalItemCount = layoutManager.itemCount;
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            if (!isLoading && isScrolling && totalItemCount <= (lastVisibleItem + 1)) {
                articleViewModel.getNextArticles(authToken)
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

}
