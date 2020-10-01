package nl.yussef.ali.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.recycleView
import kotlinx.android.synthetic.main.activity_category.swipeRefreshLayout
import nl.yussef.ali.R
import nl.yussef.ali.adapters.ArticleAdapter
import nl.yussef.ali.repository.ArticleRepository
import nl.yussef.ali.repository.SavedArticleRepository
import nl.yussef.ali.ui.viewModel.ArticleViewModel
import nl.yussef.ali.ui.viewModel.ArticleViewModelProviderFactory
import nl.yussef.ali.ui.viewModel.SavedArticleViewModel
import nl.yussef.ali.ui.viewModel.SavedArticleViewModelFactory
import nl.yussef.ali.util.LinearLayoutManagerWrapper
import nl.yussef.ali.util.Resource
import nl.yussef.ali.util.SessionManager

class CategoryActivity : AppCompatActivity() {
    private var categoryName: String = CATEGORY_NAME
    private lateinit var articleViewModel: ArticleViewModel
    lateinit var savedArticleViewModel: SavedArticleViewModel
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var sessionManager: SessionManager
    private var authToken: String? = null
    var isLoading = false
    var isScrolling = false

    companion object {
        const val CATEGORY_NAME = "categoryName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        initializeUI()

        articleViewModel.articles.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    if (response.data?.Results.isNullOrEmpty()) {
                        Toast.makeText(
                            this,
                            getString(R.string.no_articles_found),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        response.data?.let { articleResponse ->
                            articleAdapter.differ.submitList(articleResponse.Results.toList())
                        }
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
        val articleRepository = ArticleRepository()
        val articleViewModelProviderFactory =
            ArticleViewModelProviderFactory(articleRepository, this)
        articleViewModel = ViewModelProvider(
            this,
            articleViewModelProviderFactory
        ).get(ArticleViewModel::class.java)

        val savedArticleRepository = SavedArticleRepository()
        val savedArticleViewModelFactory =
            SavedArticleViewModelFactory(savedArticleRepository, this)
        savedArticleViewModel = ViewModelProvider(
            this,
            savedArticleViewModelFactory
        ).get(SavedArticleViewModel::class.java)

        categoryName = intent.getStringExtra(CATEGORY_NAME).toString()
        categoryNameText.text = categoryName
        sessionManager = SessionManager(this)
        setupRecyclerView()

        swipeRefreshLayout.setOnRefreshListener {
            articleViewModel.articles.value?.data?.Results?.clear()
            articleViewModel.getArticlesByCategory(authToken, categoryName)
            swipeRefreshLayout.isRefreshing = false
        }

        if (sessionManager.isLogin()) {
            authToken = sessionManager.getUserDetails().AuthToken.toString()
        }

        if (articleViewModel.articles.value?.data?.Results.isNullOrEmpty()) {
            articleViewModel.getArticlesByCategory(authToken, categoryName)
        }
        articleAdapter.setOnItemClickListener {
            val intent = Intent(this, DetailsArticleActivity::class.java)
            intent.putExtra(DetailsArticleActivity.ARTICLE, it)
            startActivity(intent)
        }

        articleAdapter.setOnLikedButtonClickListener { article, i ->
            if (!article.IsLiked!!) {
                savedArticleViewModel.likeAnArticle(article.Id!!, authToken!!)
                Toast.makeText(
                    this,
                    R.string.add_to_favourite,
                    Toast.LENGTH_SHORT
                ).show()
                articleAdapter.differ.currentList[i].IsLiked = true
                articleAdapter.notifyItemChanged(i)
            } else {
                savedArticleViewModel.unlikeAnArticle(article.Id!!, authToken!!)
                Toast.makeText(
                    this,
                    R.string.remove_from_favourite,
                    Toast.LENGTH_SHORT
                ).show()
                articleAdapter.differ.currentList[i].IsLiked = false
                articleAdapter.notifyItemChanged(i)
            }
        }
    }

    private fun hideProgressBar() {
        paginationProgressBarCategoryActivity.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        paginationProgressBarCategoryActivity.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter()
        recycleView.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManagerWrapper(this@CategoryActivity)
            addOnScrollListener(this@CategoryActivity.scrollListener)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val totalItemCount = layoutManager.itemCount;
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            if (!isLoading && isScrolling && totalItemCount <= (lastVisibleItem + 1)) {
                articleViewModel.getNextArticlesByCategory(authToken, categoryName)
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