package ru.skillbranch.skillarticles.viewmodels.articles

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleDataFactory
import ru.skillbranch.skillarticles.data.repositories.ArticleStrategy
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.extensions.logd
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors


class ArticlesViewModel(handle: SavedStateHandle) : BaseViewModel<ArticlesState>(handle, ArticlesState()) {
    val repository = ArticlesRepository
    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(30)
            .setInitialLoadSizeHint(50)
            .build()
    }

    private val listData = Transformations.switchMap(state){
        when {
            it.isSearch && !it.searchQuery.isNullOrBlank() -> buildPagedList(repository.searchArticles(it.searchQuery))
            else -> buildPagedList(repository.allArticles())
        }

    }



    fun observeList(
        owner: LifecycleOwner,
        onChange : (list: PagedList<ArticleItemData>) -> Unit
    ){
        listData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPagedList(
        dataFactory : ArticleDataFactory
    ) : LiveData<PagedList<ArticleItemData>>{
        val builder = LivePagedListBuilder<Int, ArticleItemData>(
            dataFactory, listConfig
        )

        if(dataFactory.strategy is ArticleStrategy.AllArticles)
            builder.setBoundaryCallback(ArticleBoundaryCallback(
                ::zeroLoadingHandle,
                ::itemAtEndHandle
            ))
        return builder
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    private fun itemAtEndHandle(lastLoadArticle: ArticleItemData) {
        logd()
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(
                start = lastLoadArticle.id.toInt().inc(),
                size = listConfig.pageSize
            )

            if(items.isNotEmpty()){
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }

            withContext(Dispatchers.Main){
                notify(Notify.TextMessage("Load from network articles " +
                        "from ${items.firstOrNull()?.id} to ${items.lastOrNull()?.id}"))
            }
        }
    }

    private fun zeroLoadingHandle() {
        logd()
        notify(Notify.TextMessage("Storage is empty"))
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(start = 0, size = listConfig.initialLoadSizeHint )
            if(items.isNotEmpty()){
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }
        }
    }

    fun handleSearch(query: String?) {
        query ?: return
        updateState { it.copy(searchQuery = query) }
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState {
            it.copy(isSearch = isSearch)
        }
    }

    fun handleToggleBookmark(articleId: String, hasBookmark: Boolean): Unit {
        updateState { it.copy(isLoading = true) }
        repository.updateBookmark(articleId, !hasBookmark)
        updateState { it.copy(isLoading = false) }
    }

}

data class ArticlesState(
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val isLoading: Boolean = true
): IViewModelState


class ArticleBoundaryCallback(
    private val zeroLoadingHandle: () -> Unit,
    private val itemAtEndHandle: (ArticleItemData) -> Unit

) : PagedList.BoundaryCallback<ArticleItemData>() {

    override fun onZeroItemsLoaded() {
        zeroLoadingHandle()
    }

    override fun onItemAtEndLoaded(itemAtEnd: ArticleItemData) {
        itemAtEndHandle(itemAtEnd)
    }
}
