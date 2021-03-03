package ru.skillbranch.skillarticles.extensions

/*
мы обрабатываем уже готовый результат поиска List<Pair<Int, Int>>
нам нужно разбить (сгруппировать) этот результат по имеющимся текстовым элементам (bounds: List<Pair<Int, Int>>)
получаем в итоге список результатов поиска List<List<Pair<Int, Int>>>
*/
fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>) : List<List<Pair<Int, Int>>> {
    val resultList = mutableListOf<List<Pair<Int, Int>>>()
    bounds.forEach {(leftBound, rightBound) ->
        val inBounds = this.filter { (lb, rb) ->
            lb >= leftBound && rb <= rightBound
        }
        resultList.add(inBounds)
    }
    return resultList
}
