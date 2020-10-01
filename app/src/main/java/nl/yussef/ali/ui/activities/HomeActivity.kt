package nl.yussef.ali.ui.activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_home.*
import nl.yussef.ali.R
import nl.yussef.ali.repository.ArticleRepository
import nl.yussef.ali.repository.SavedArticleRepository
import nl.yussef.ali.repository.UserRepository
import nl.yussef.ali.ui.viewModel.*
import nl.yussef.ali.util.SessionManager

class HomeActivity : AppCompatActivity() {
    lateinit var articleViewModel: ArticleViewModel
    lateinit var savedArticleViewModel: SavedArticleViewModel
    lateinit var userViewModel: UserViewModel
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        sessionManager = SessionManager(this)

        initializeViewModels()

        bottomNavigationView.setupWithNavController(articleNavHostFragment.findNavController())
    }

    private fun initializeViewModels() {
        val articleRepository = ArticleRepository()
        val viewModelProviderFactory = ArticleViewModelProviderFactory(articleRepository, this)
        articleViewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(ArticleViewModel::class.java)

        val savedArticleRepository = SavedArticleRepository()
        val savedArticleViewModelFactory =
            SavedArticleViewModelFactory(savedArticleRepository, this)
        savedArticleViewModel = ViewModelProvider(
            this,
            savedArticleViewModelFactory
        ).get(SavedArticleViewModel::class.java)

        val userRepository = UserRepository()
        val userViewModelProviderFactory = UserViewModelProviderFactory(userRepository, this)
        userViewModel =
            ViewModelProvider(this, userViewModelProviderFactory).get(UserViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.userAction -> {
                if (!sessionManager.isLogin()) {
                    navigateToSelectedFragment("LoginFragment")

                } else if (sessionManager.isLogin()) {
                    navigateToSelectedFragment("WelcomeFragment")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getNavOptions(): NavOptions {
        return NavOptions.Builder().setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
            .setPopUpTo(R.id.welcomeFragment, true)
            .build();
    }

    private fun navigateToSelectedFragment(fragmentName: String) {
        val fragment =
            if (fragmentName == "LoginFragment") R.id.loginFragment else R.id.welcomeFragment

        val isFragmentAlreadyThere =
            NavHostFragment.findNavController(articleNavHostFragment).currentDestination?.label?.equals(
                fragmentName
            )

        isFragmentAlreadyThere?.let {
            if (!it) {
                NavHostFragment.findNavController(articleNavHostFragment)
                    .navigate(fragment, null, getNavOptions())
            } else {
                NavHostFragment.findNavController(articleNavHostFragment)
                    .popBackStack(fragment, true)
            }
        }
    }

}