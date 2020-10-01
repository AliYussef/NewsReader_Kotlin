package nl.yussef.ali.util

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.LinearLayoutManager

class LinearLayoutManagerWrapper(context: Context?) : LinearLayoutManager(context) {

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}