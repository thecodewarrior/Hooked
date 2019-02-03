package thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.kotlin.toRl

object HookTypes {
    val missingno = BasicHookType(
            name = "missingno".toRl(),
            count = 0, range = 0.0,
            speed = 0.0, pullStrength = 0.0,
            hookLength = 0.0
    )
    val wood = BasicHookType(
            name = "hooked:wood".toRl(),
            count = 1, range = 8.0,
            speed = 8/20.0, pullStrength = 4/20.0,
            hookLength = 0.5
    )
    val iron = BasicHookType(
            name = "hooked:iron".toRl(),
            count = 2, range = 16.0,
            speed = 16/20.0, pullStrength = 8/20.0,
            hookLength = 0.5
    )
    val diamond = BasicHookType(
            name = "hooked:diamond".toRl(),
            count = 4, range = 24.0,
            speed = 24/20.0, pullStrength = 20/20.0,
            hookLength = 0.5
    )
    val red = FlightHookType(
            name = "hooked:red".toRl(),
            count = 4, range = 24.0,
            speed = 24/20.0,
            hookLength = 0.5
    )
    val ender = BasicHookType(
            name = "hooked:ender".toRl(),
            count = 1, range = 64.0,
            speed = 64.0, pullStrength = 45/20.0,
            hookLength = 0.5
    )
}