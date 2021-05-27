package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.extensions.logd
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class RootViewModel(handle: SavedStateHandle) : BaseViewModel<RootState>(handle, RootState()) {
    private val repository = RootRepository
    private val privateRoutes = listOf(R.id.nav_profile)

    init {
        subscribeOnDataSource(repository.isAuth()) { isAuth, state ->
            logd("state.copy(isAuth = $isAuth)", "myTest")
            state.copy(isAuth = isAuth)
        }

        logd("subscribeOnDataSource(repository.isAuth()); repository: $repository, repository.isAuth(): ${repository.isAuth()}, repository.isAuth()=${repository.isAuth().value}")
    }

    //создание события навигации для любой навигации (через BottomNavigationView listener или напрямое создание события навигации)
    override fun navigate(command: NavigationCommand) {
        when (command) {
            is NavigationCommand.To -> {
                //на логин если переходим на nav_profile и нет авторизации
                logd("StartLogin repository.isAuth()=${repository.isAuth()}")
                if (privateRoutes.contains(command.destination)) {
                    //ситуация, когда уже авторизованы перехватывается в addOnDestinationChangedListener
                    logd("StartLogin")
                    super.navigate(NavigationCommand.StartLogin(command.destination))
                } else {
                    super.navigate(command)
                }
            }
            else -> super.navigate(command)
        }
    }
}

data class RootState(
    val isAuth: Boolean = false
) : IViewModelState