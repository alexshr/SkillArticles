package ru.skillbranch.skillarticles.ui.base

import android.os.Bundle
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import kotlin.reflect.KProperty

abstract class Binding {

    val delegates = mutableMapOf<String, RenderProp<out Any>>()

    abstract fun onFinishInflate()

    abstract fun bind(data: IViewModelState)

    abstract fun saveUi(outState: Bundle)

    abstract fun restoreUi(savedState: Bundle)

    /* В RenderProp delegate каждого поля добавляется общий для всех полей listener
      (до этого там уже был свой индивидуальный)
      ф-я onChange: (A, B, C, D) -> Unit  принимает типы 4-х полей,
      а сами поля берет из поля delegates (map)

      generic параметры типов переходят в аргументы функции
      */
    @Suppress("UNCHECKED_CAST")
    fun <A, B, C, D> dependsOn(
        vararg fields: KProperty<*>,
        onChange: (A, B, C, D) -> Unit
    ) {
        check(fields.size == 4) { "Names size must be 4, current ${fields.size}" }
        val names = fields.map { it.name }

        names.forEach { name ->
            delegates[name]?.addListener {
                onChange(
                    delegates[names[0]]?.value as A,
                    delegates[names[1]]?.value as B,
                    delegates[names[2]]?.value as C,
                    delegates[names[3]]?.value as D
                )
            }
        }
    }
}
