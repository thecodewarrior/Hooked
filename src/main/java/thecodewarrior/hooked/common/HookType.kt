package thecodewarrior.hooked.common

/**
 * Created by TheCodeWarrior
 */
enum class HookType(val count: Int, val range: Int, firingBlocksPerSecond: Int, reelSpeed: Int, val hookLength: Double) {
    WOOD(1, 8, 8, 2, 0.5),
    IRON(2, 16, 16, 5, 0.5),
    DIAMOND(4, 24, 24, 20, 0.5),
    RED(4, 24, 24, 20, 0.5),
    ENDER(3, 64, -1, 45, 0.5);

    val speed = if (firingBlocksPerSecond == -1) range.toDouble() else firingBlocksPerSecond / 20.0
    val rangeSq = range * range
    val pullStrength = reelSpeed / 20.0

}
