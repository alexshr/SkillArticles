package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.github.ajalt.timberkt.Timber
import com.google.android.material.snackbar.Snackbar
import com.wada811.databinding.dataBinding
import kotlinx.android.synthetic.main.activity_root.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.TimberExtensions.init
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity(R.layout.activity_root) {

    private val binding: ActivityRootBinding by dataBinding()
    private val viewModel by viewModels<ArticleViewModel> { ViewModelFactory("0") }

    private lateinit var mSearchView: SearchView
    private var mSearchItem: MenuItem? = null
    /*private var searchQuery : String? = null
    private var isSearchExpanded = false

    private val SEARCH_TEXT_KEY = "search_query"
    private val SEARCH_EXPAND_KEY = "search_expand"*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) Timber.init()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        binding.model = viewModel
        binding.lifecycleOwner = this
        binding.bottombar.setupViewModel(viewModel, this)
        binding.submenu.setupViewModel(viewModel, this)


        //setupBottombar()
        //setupSubmenu()
        /*if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_TEXT_KEY);
            isSearchExpanded = savedInstanceState.getBoolean(SEARCH_EXPAND_KEY);
        }*/
        viewModel.observeNotifications(this) {
            renderNotification(it)
        }


    }

    override fun onStop() {
        viewModel.handleSearchMode(mSearchItem?.isActionViewExpanded ?: false)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        mSearchItem = menu?.findItem(R.id.action_search)
        mSearchView = mSearchItem?.actionView as SearchView
        mSearchView.apply {
            queryHint = "Search"
            maxWidth = Int.MAX_VALUE
        }

        if (viewModel.getSearchMode()) {
            mSearchItem?.expandActionView()
            mSearchView.setQuery(viewModel.getSearchQuery(), true)
        }

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)


        when (notify) {
            is Notify.TextMessage -> {
            }
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }
}




