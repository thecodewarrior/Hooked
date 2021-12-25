package dev.thecodewarrior.hooked.hook

import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import java.util.UUID

data class HookEvent(
    val type: EventType,
    val id: Int,
    val data: Int,
) {

    enum class EventType {
        HIT, MISS, DISLODGE
    }
}