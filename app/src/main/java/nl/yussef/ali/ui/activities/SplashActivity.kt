package nl.yussef.ali.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_splash.*
import nl.yussef.ali.R

class SplashActivity : AppCompatActivity() {
    companion object {
        private const val SPLASH_TIME_OUT: Long = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        newsReaderLogo3.animate().alpha(0f).setDuration(800).startDelay = 2100
        newsReaderLogo.animate().translationX(-800f).setDuration(800).startDelay = 2100
        newsReaderLogo2.animate().translationX(800f).setDuration(800).startDelay = 2100

        Handler().postDelayed({
            startActivity(
                Intent(this, HomeActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
            finish()
        }, SPLASH_TIME_OUT)
    }
}