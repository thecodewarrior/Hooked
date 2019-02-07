package games.thecodewarrior.hooked.common

/**
 * Created by TheCodeWarrior
 */
enum class HookTypeEnum(val count: Int, val range: Int, blocksPerSecond: Int, reelSpeed: Int, val hookLength: Double) {
    WOOD(
            count = 1, range = 8,
            blocksPerSecond = 8, reelSpeed = 4,
            hookLength = 0.5
    ),
    IRON(
            count = 2, range = 16,
            blocksPerSecond = 16, reelSpeed = 8,
            hookLength = 0.5
    ),
    DIAMOND(
            count = 4, range = 24,
            blocksPerSecond = 24, reelSpeed = 20,
            hookLength = 0.5
    ),
    RED(
            count = 4, range = 24,
            blocksPerSecond = 24, reelSpeed = 20,
            hookLength = 0.5
    ),
    ENDER(
            count = 1, range = 64,
            blocksPerSecond = -1, reelSpeed = 45,
            hookLength = 0.5
    );

    val speed = if (blocksPerSecond == -1) range.toDouble() else blocksPerSecond / 20.0
    val rangeSq = range * range
    val pullStrength = reelSpeed / 20.0

}
