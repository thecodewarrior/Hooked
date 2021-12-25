package dev.thecodewarrior.hooked.hook

import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import java.util.UUID

@RefractClass
data class HookEvent @RefractConstructor constructor(
    @Refract("type") val type: EventType,
    @Refract("id") val id: Int,
    @Refract("data") val data: Int,
) {

    enum class EventType {
        HIT, MISS, DISLODGE
    }
}