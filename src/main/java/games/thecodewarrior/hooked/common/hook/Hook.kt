package games.thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.saving.Savable
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*

/**
 * The data for a planted hook
 */
@Savable
data class Hook(
        /**
         * The position of the _back_ of the hook. (add direction*hookLength to get the tip)
         */
        override var pos: Vec3d,
        /**
         * The direction the hook is facing
         */
        override var direction: Vec3d,
        /**
         * The block the hook is attached to
         */
        var block: BlockPos,
        /**
         * The side the hook is attached to
         */
        var side: EnumFacing,
        /**
         * The UUID of the hook
         */
        override val uuid: UUID
): IHook
