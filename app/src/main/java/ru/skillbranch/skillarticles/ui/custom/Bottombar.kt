package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.BottombarBinding
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel

class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {
    init {
        View.inflate(context, R.layout.bottombar, this)
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    private var binding: BottombarBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(getContext()),
            R.layout.bottombar,
            this,
            true
        )

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return BottombarBehavior()
    }

    /*fun renderUI(data: ArticleState) {
        binding.apply {
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }
    }*/

    fun setupViewModel(viewModel: ViewModel, lifecycleowner: LifecycleOwner) {
        binding.apply {
            model = viewModel as ArticleViewModel?
            lifecycleOwner = lifecycleowner
        }
    }
}