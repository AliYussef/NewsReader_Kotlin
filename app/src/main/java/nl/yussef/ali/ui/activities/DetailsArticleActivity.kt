package nl.yussef.ali.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_details_article.*
import kotlinx.android.synthetic.main.activity_home.*
import nl.yussef.ali.R
import nl.yussef.ali.model.Article
import nl.yussef.ali.repository.SavedArticleRepository
import nl.yussef.ali.ui.viewModel.SavedArticleViewModel
import nl.yussef.ali.ui.viewModel.SavedArticleViewModelFactory
import nl.yussef.ali.util.SessionManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class DetailsArticleActivity : AppCompatActivity() {

    private lateinit var article: Article
    private val args: DetailsArticleActivityArgs by navArgs()
    private lateinit var sessionManager: SessionManager
    lateinit var savedArticleViewModel: SavedArticleViewModel

    companion object {
        const val ARTICLE = "article"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_article)

        val savedArticleRepository = SavedArticleRepository()
        val savedArticleViewModelFactory =
            SavedArticleViewModelFactory(savedArticleRepository, this)
        savedArticleViewModel = ViewModelProvider(
            this,
            savedArticleViewModelFactory
        ).get(SavedArticleViewModel::class.java)


        sessionManager = SessionManager(this)
        initializeUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeUI() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }
        collapsing_toolbar.title = ""

        article = args.article ?: (intent.getSerializableExtra(ARTICLE) as Article)
        titleDetails.text = article.Title
        summary.apply {
            text = article.Summary
            setLinkTextColor(Color.MAGENTA)
        }
        Linkify.addLinks(summary, Linkify.WEB_URLS)
        readMore(readMoreBtn)

        article.Categories?.forEach { category ->
            val btn = Button(this)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                height = 100
                setMargins(0, 0, 30, 30)
            }
            btn.apply {
                text = category.Name
                setBackgroundResource(R.drawable.button_category)
                id = category.Id?.toInt()!!
                setTextColor(Color.WHITE)
                textSize = 14f
                setPadding(20)
                layoutParams = params
                transformationMethod = null
                setOnClickListener {
                    val intent = Intent(this.context, CategoryActivity::class.java)
                    intent.putExtra(CategoryActivity.CATEGORY_NAME, text.toString())
                    finish()
                    startActivity(intent)
                }
            }
            frameButton.addView(btn)
        }

        article.Related?.forEach { related ->
            val textView = TextView(this)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
            textView.apply {
                text = related
                setTextColor(Color.MAGENTA)
                setLinkTextColor(Color.MAGENTA)
                layoutParams = params
            }
            Linkify.addLinks(textView, Linkify.WEB_URLS)
            relatedNewsLay.addView(textView)
        }

        date.text = LocalDateTime.parse(article.PublishDate).toLocalDate().format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())
        ).toString()

        Glide.with(this).load(article.Image)
            .apply(RequestOptions.errorOf(R.drawable.ic_baseline_image_24))
            .transition(DrawableTransitionOptions.withCrossFade()).into(backdrop)

        activateLikeButton()
        toolbar.navigationIcon?.setTint(Color.WHITE)
    }

    private fun likeOrUnlikeArticle() {
        val authToken = sessionManager.getUserDetails().AuthToken.toString()
        if (article.IsLiked!!) {
            savedArticleViewModel.unlikeAnArticle(article.Id!!, authToken)
            article.IsLiked = false
            likeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            Toast.makeText(
                this,
                R.string.remove_from_favourite,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            savedArticleViewModel.likeAnArticle(article.Id!!, authToken)
            article.IsLiked = true
            likeButton.setImageResource(R.drawable.ic_baseline_favorite_24)
            Toast.makeText(
                this,
                R.string.add_to_favourite,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun activateLikeButton() {
        if (sessionManager.isLogin()) {
            likeButton.apply {
                visibility = View.VISIBLE
                if (article.IsLiked!!) setImageResource(R.drawable.ic_baseline_favorite_24)
                setOnClickListener { likeOrUnlikeArticle() }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_page_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.shareAction -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, article.Title + " \n" + article.Url)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }

    fun readMore(view: View) {
        readMoreBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(article.Url)
            startActivity(intent)
        }
    }
}