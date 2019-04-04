@file:JvmName("MixinUtils")
package games.thecodewarrior.hooked.mixinsupport

@Suppress("UNCHECKED_CAST")
fun <T> Any.mCast(): T = this as T