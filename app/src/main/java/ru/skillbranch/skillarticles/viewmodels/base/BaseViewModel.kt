package ru.skillbranch.skillarticles.viewmodels.base

import android.os.Bundle
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import ru.skillbranch.skillarticles.extensions.logd

abstract class BaseViewModel<T : IViewModelState>(
    private val handleState: SavedStateHandle,
    initState: T
) : ViewModel() {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val notifications = MutableLiveData<Event<Notify>>()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val navigation = MutableLiveData<Event<NavigationCommand>>()

    /***
     * Инициализация начального состояния аргументом конструктоа, и объявления состояния как
     * MediatorLiveData - медиатор исспользуется для того чтобы учитывать изменяемые данные модели
     * и обновлять состояние ViewModel исходя из полученных данных
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val state: MediatorLiveData<T> = MediatorLiveData<T>().apply {
        value = initState
    }

    /***
     * getter для получения not null значения текущего состояния ViewModel
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val currentState
        get() = state.value!!


    /***
     * лямбда выражение принимает в качестве аргумента текущее состояние и возвращает
     * модифицированное состояние, которое присваивается текущему состоянию
     */
    @UiThread
    protected inline fun updateState(update: (currentState: T) -> T) {
        val updatedState: T = update(currentState)
        state.value = updatedState
    }

    /***
     * функция для создания уведомления пользователя о событии (событие обрабатывается только один раз)
     * соответсвенно при изменении конфигурации и пересоздании Activity уведомление не будет вызвано
     * повторно
     */
    @UiThread
    protected fun notify(content: Notify) {
        notifications.value = Event(content)
    }

    //создаем событие навигации
    open fun navigate(command: NavigationCommand) {
        navigation.value = Event(command)
    }

    /***
     * более компактная форма записи observe() метода LiveData принимает последним аргумент лямбда
     * выражение обрабатывающее изменение текущего стостояния
     */
    fun observeState(owner: LifecycleOwner, onChanged: (newState: T) -> Unit) {
        state.observe(owner, Observer { onChanged(it!!) })
    }

    /***
     * более компактная форма записи observe() метода LiveData вызывает лямбда выражение обработчик
     * только в том случае если уведомление не было уже обработанно ранее,
     * реализует данное поведение с помощью EventObserver
     */
    fun observeNotifications(owner: LifecycleOwner, onNotify: (notification: Notify) -> Unit) {
        notifications.observe(owner,
            EventObserver { onNotify(it) })
    }

    fun observeNavigation(owner: LifecycleOwner, onNavigate: (command: NavigationCommand) -> Unit) {
        navigation.observe(owner,
            EventObserver { onNavigate(it) })
    }

    /* К общему MediatorLiveData добавляем некоторый source: LiveData<S>
       При изменении состояния source (set, post) срабатывает обработчик общего MediatorLiveData
       где уже он сам изменяет состояние state.value = onChanged(it, currentState)
       onChange возвращает новое значение для state (MediatorLiveData)
       в onChange передаются некие аргументы (newValue: S, currentState: T)
       на практике в onChange происходит изменение состояния (currentState) путем присвоения
       одному из его полей нового текущего значения (newValue) ливдаты source

       Т.е. как только изменяется source его новое значение (например content) обновляет некий общий
       state (MediatorLiveData) на который подписаны все view
       */
    protected fun <S> subscribeOnDataSource(
        source: LiveData<S>,
        onChanged: (newValue: S, currentState: T) -> T?
    ) {
        state.addSource(source) {
            logd("old state.value: ${state.value}")
            state.value = onChanged(it, currentState) ?: return@addSource
            logd("new state.value: ${state.value}")
        }
        logd("addSource: $source")

    }

    fun saveState() {
        currentState.save(handleState)
    }

    @Suppress("UNCHECKED_CAST")
    fun restoreState() {
        state.value = currentState.restore(handleState) as T
    }

}

class Event<out E>(private val content: E) {
    var hasBeenHandled = false

    /***
     * возвращает контент который еще не был обработан иначе null
     */
    fun getContentIfNotHandled(): E? {
        return if (hasBeenHandled) null
        else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): E = content
}

/***
 * в качестве аргумента конструктора принимает лямбда выражение обработчик в аргумент которой передается
 * необработанное ранее событие получаемое в реализации метода Observer`a onChanged
 */
class EventObserver<E>(private val onEventUnhandledContent: (E) -> Unit) : Observer<Event<E>> {

    override fun onChanged(event: Event<E>?) {
        //если есть необработанное событие (контент) передай в качестве аргумента в лямбду
        // onEventUnhandledContent
        event?.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}

sealed class Notify() {
    abstract val message: String

    data class TextMessage(override val message: String) : Notify()

    data class ActionMessage(
        override val message: String,
        val actionLabel: String,
        val actionHandler: (() -> Unit)
    ) : Notify()

    data class ErrorMessage(
        override val message: String,
        val errLabel: String?,
        val errHandler: (() -> Unit)?
    ) : Notify()
}

sealed class NavigationCommand() {
    data class To(
        val destination: Int,
        val args: Bundle? = null,
        val options: NavOptions? = null,
        val extras: Navigator.Extras? = null
    ) : NavigationCommand()

    data class StartLogin(
        val privateDestination: Int? = null
    ) : NavigationCommand()

    data class FinishLogin(
        val privateDestination: Int? = null
    ) : NavigationCommand()
}