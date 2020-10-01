package nl.yussef.ali.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kotlinx.android.synthetic.main.recycle_view_item.view.*
import nl.yussef.ali.R
import nl.yussef.ali.model.Article
import nl.yussef.ali.util.SessionManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {
    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sessionManager: SessionManager = SessionManager(itemView.context)
    }

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycle_view_item,
                parent,
                false
            )
        )
    }

    private var onItemClickListener: ((Article) -> Unit)? = null
    private var onLikedButtonClickListener: ((Article, Int) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.itemView.apply {
            if (holder.sessionManager.isLogin()) {
                likeButton.visibility = View.VISIBLE
                if (article.IsLiked == true) {
                    likeButton.setImageResource(R.drawable.ic_baseline_favorite_24)
                } else {
                    likeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
            }
            likeButton.setOnClickListener {
                val pos: Int = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onLikedButtonClickListener?.let {
                        it(article, pos)
                    }
                }
            }
            imageCard.load(article.Image) {
                crossfade(true)
                placeholder(R.drawable.ic_baseline_image_24)
            }
            title.text = article.Title
            date.text = LocalDateTime.parse(article.PublishDate).toLocalDate().format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())
            ).toString()

            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnLikedButtonClickListener(listener: (Article, Int) -> Unit) {
        onLikedButtonClickListener = listener
    }

}