package thecodewarrior.hooked.common.block

import com.teamwizardry.librarianlib.features.base.block.BlockMod
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockRenderLayer

/**
 * Created by TheCodeWarrior
 */
class BlockBalloon : BlockMod("balloon", Material.CLOTH, *EnumDyeColor.values().map { "balloon_" + it.unlocalizedName }.toTypedArray()) {

    init {

    }

    override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, COLOR)
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        return defaultState.withProperty(COLOR, EnumDyeColor.values()[meta])
    }

    override fun getMetaFromState(state: IBlockState): Int {
        return state.getValue(COLOR).ordinal
    }

    override fun getBlockLayer(): BlockRenderLayer {
        return BlockRenderLayer.CUTOUT
    }

    override fun damageDropped(state: IBlockState): Int {
        return state.getValue(COLOR).metadata
    }

    override fun isOpaqueCube(state: IBlockState?): Boolean {
        return false
    }

    companion object {
        val COLOR = PropertyEnum.create("color", EnumDyeColor::class.java)
    }
}
