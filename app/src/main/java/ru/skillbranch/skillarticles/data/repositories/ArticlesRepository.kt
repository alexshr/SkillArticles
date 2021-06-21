package ru.skillbranch.skillarticles.data.repositories

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.extensions.logd
import ru.skillbranch.skillarticles.extensions.logi
import java.lang.Thread.sleep

/*
Получение DataFactory по конкретной стратегии
*/
object ArticlesRepository {

    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    //region получение нужных DataFactory (pagging library)
    fun allArticles(): ArticleDataFactory =
        ArticleDataFactory(ArticleStrategy.AllArticles(::findArticlesByRange))

    fun searchArticles(searchQuery: String) =
        ArticleDataFactory(ArticleStrategy.SearchArticle(::searchArticlesByTitle, searchQuery))

    fun allBookmarks(): ArticleDataFactory =
        ArticleDataFactory(ArticleStrategy.BookmarkArticles(::findArticlesBookmark))

    fun searchBookmarks(searchQuery: String): ArticleDataFactory =
        ArticleDataFactory(
            ArticleStrategy.SearchBookmarks(::findBookmarkArticlesByTitle, searchQuery)
        )
    //endregion


    //region получение локальных для нужных DataFactory (pagging library)
    private fun findArticlesByRange(start: Int, size: Int) = local.localArticleItems
        .drop(start)
        .take(size)

    private fun searchArticlesByTitle(start: Int, size: Int, queryTitle: String) =
        local.localArticleItems
            .asSequence()
            .filter { it.title.contains(queryTitle, true) }
            .drop(start)
            .take(size)
            .toList()

    private fun findArticlesBookmark(start: Int, size: Int) = local.localArticleItems
        .asSequence()
        .filter { it.isBookmark }
        .drop(start)
        .take(size)
        .toList()

    private fun findBookmarkArticlesByTitle(start: Int, size: Int, queryTitle: String) =
        local.localArticleItems
            .asSequence()
            .filter { it.isBookmark && it.title.contains(queryTitle, true) }
            .drop(start)
            .take(size)
            .toList()
    //endregion

    //загрузка данных из сети
    fun loadArticlesFromNetwork(start: Int, size: Int): List<ArticleItemData> =
        network.networkArticleItems
            .drop(start)
            .take(size)
            .apply {
                sleep(500)
            }

    fun insertArticlesToDb(articles: List<ArticleItemData>) {
        local.localArticleItems.addAll(articles)
            .apply {
                sleep(500)
            }
    }

    fun updateBookmark(articleId: String, bookmark: Boolean) {
        local.localArticleItems
            .indexOfFirst { it.id == articleId }
            .takeUnless { it == -1 }
            ?.let { index ->
                local.localArticleItems[index] =
                    local.localArticleItems[index].copy(isBookmark = bookmark)
            }
        logd("articleId $articleId bookmark $bookmark")
    }

}

//получение datasource из ArticlesRepository по нужной стратегии (там указан метод репозитория)
class ArticleDataFactory(val strategy: ArticleStrategy) :
    DataSource.Factory<Int, ArticleItemData>() {
    override fun create(): DataSource<Int, ArticleItemData> = ArticleDataSource(strategy)

}

class ArticleDataSource(val strategy: ArticleStrategy) : PositionalDataSource<ArticleItemData>() {
    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ArticleItemData>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        logi(
            "loadInitial: start = ${params.requestedStartPosition} " +
                    "size = ${params.requestedLoadSize} resultSize ? ${result.size}"
        )
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        logi(
            "loadRange: start = ${params.startPosition} size = ${params.loadSize}, resultSize ? ${result.size}"
        )
        callback.onResult(result)
    }

}

//каждая стратегия получает метод получения items
sealed class ArticleStrategy() {
    abstract fun getItems(start: Int, size: Int): List<ArticleItemData>

    class AllArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)

    }

    class SearchArticle(
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)

    }

    class BookmarkArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)
    }

    class SearchBookmarks(
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)

    }
}