package dev.thecodewarrior.hooked.hook.type

class BasicHookType(
    override val count: Int,
    override val range: Double,
    override val speed: Double,
    override val pullStrength: Double,
    override val hookLength: Double,
    override val jumpBoost: Double
): HookType() {
}