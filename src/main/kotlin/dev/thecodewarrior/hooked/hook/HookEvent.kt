package dev.thecodewarrior.hooked.hook

data class HookEvent(
    val type: EventType,
    val id: Int,
    val data: Int,
) {

    enum class EventType {
        HIT, MISS, DISLODGE
    }
}