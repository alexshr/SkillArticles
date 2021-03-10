package ru.skillbranch.skillarticles.viewmodels.auth

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.extensions.logd
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class AuthViewModel(handle: SavedStateHandle) : BaseViewModel<AuthState>(handle,AuthState()), IAuthViewModel {
    private val repository = RootRepository
    init {
        //logd("repository: $repository")
        /*subscribeOnDataSource(repository.isAuth()){isAuth, state ->
            state.copy(isAuth = isAuth)
        }*/
    }

    override fun handleLogin(login: String, pass: String, dest: Int?){

        repository.setAuth(true)
        logd("repository: $repository setAuth: true")
        navigate(NavigationCommand.FinishLogin(dest))
    }
}


data class AuthState(val isAuth: Boolean = false) : IViewModelState