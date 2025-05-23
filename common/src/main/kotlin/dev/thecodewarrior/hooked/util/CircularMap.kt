package dev.thecodewarrior.hooked.util

/**
 * A map that drops the oldest elements its size exceeds [maxSize]
 */
class CircularMap<K, V>(val maxSize: Int) : LinkedHashMap<K, V>(maxSize, 0.75f, false) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }
}