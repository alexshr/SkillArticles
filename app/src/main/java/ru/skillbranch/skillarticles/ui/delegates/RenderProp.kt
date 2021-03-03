package ru.skillbranch.skillarticles.ui.delegates

import ru.skillbranch.skillarticles.ui.base.Binding
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*К полю класса thisRef: Binding данный делегат добавляет функцию onChange (при объявлении данного делегата),
  автоматически запускаемую при вызове setter и инициализации (если needInit==true)
  Кроме того для делегата можно добавить любое число дополнительных listeners (fun addListener(listener: () -> Unit))
*/
class RenderProp<T : Any>(
    var value: T,
    private val needInit: Boolean = true,
    private val onChange: ((T) -> Unit)? = null
) : ReadWriteProperty<Binding, T> {

    private val listeners: MutableList<() -> Unit> = mutableListOf()
    fun bind() {
        if (needInit) onChange?.invoke(value)
    }

    operator fun provideDelegate(
        thisRef: Binding,
        prop: KProperty<*>
    ): ReadWriteProperty<Binding, T> {
        val delegate = RenderProp(value, needInit, onChange)
        registerDelegate(thisRef, prop.name, delegate)
        return delegate
    }

    override fun getValue(thisRef: Binding, property: KProperty<*>): T = value

    override fun setValue(thisRef: Binding, property: KProperty<*>, value: T) {
        if (value == this.value) return
        this.value = value
        onChange?.invoke(this.value)
        if (listeners.isNotEmpty()) listeners.forEach { it.invoke() }
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    /*thisRef: Binding - объект, поле которого представляет данный делегат
    name: String  - название свойства которое мы делегируем
    delegate: RenderProp<T> - сам делегат

    Кладем делегат в общий map класса Binding,
    чтобы повесить на все поля (делегаты)общий listener*/
    private fun registerDelegate(thisRef: Binding, name: String, delegate: RenderProp<T>) {
        thisRef.delegates[name] = delegate
    }

}
