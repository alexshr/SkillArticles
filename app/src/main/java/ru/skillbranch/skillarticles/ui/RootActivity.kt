package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.logd
import ru.skillbranch.skillarticles.extensions.selectDestination
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.RootState
import ru.skillbranch.skillarticles.viewmodels.RootViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class RootActivity : BaseActivity<RootViewModel>() {
    override val layout: Int = R.layout.activity_root
    public override val viewModel: RootViewModel by viewModels()
    private var isAuth = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appbarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_articles,
                R.id.nav_bookmarks,
                R.id.nav_transcriptions,
                R.id.nav_profile
            )
        )

        setupActionBarWithNavController(navController, appbarConfiguration)
        //nav_view.setupWithNavController(navController)
        nav_view.setOnNavigationItemSelectedListener {
            viewModel.navigate(NavigationCommand.To(it.itemId))
            true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            nav_view.selectDestination(destination)//выделяем позицию на Bottom Toolbar

            /*если нас отправили на авторизацию, а мы уже авторизированны,
            то возвращаемся по backstack,
            а если на логин попали из-за того, что другая страница, потребовала авторизацию,
            то идем на ту страницу
            */
            if (isAuth && destination.id == R.id.nav_auth) {
                logd("avoid auth for authorized")
                controller.popBackStack()
                val privateDestination = arguments?.get("private_destination") as Int?
                privateDestination?.let { controller.navigate(it) }
            }
        }


    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(container, notify.message, Snackbar.LENGTH_LONG)

        snackbar.anchorView = bottombar ?: nav_view

        when (notify) {
            is Notify.TextMessage -> {
            }
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler?.invoke()
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

    override fun subscribeOnState(state: IViewModelState) {
        isAuth = (state as RootState).isAuth
    }


}
