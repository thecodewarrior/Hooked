package thecodewarrior.hooked.common.util

import net.minecraft.client.Minecraft
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

fun Double.finiteOrDefault(defaultValue: Double) = if(this.isFinite()) this else defaultValue
fun Float.finiteOrDefault(defaultValue: Float) = if(this.isFinite()) this else defaultValue

fun Double.finiteOrDefault(defaultValue: () -> Double) = if(this.isFinite()) this else defaultValue()
fun Float.finiteOrDefault(defaultValue: () -> Float) = if(this.isFinite()) this else defaultValue()

@Suppress("FunctionName")
@SideOnly(Side.CLIENT)
fun Minecraft(): Minecraft = Minecraft.getMinecraft()

fun Vec3d.isFinite(): Boolean {
    return x.isFinite() || y.isFinite() || z.isFinite()
}
