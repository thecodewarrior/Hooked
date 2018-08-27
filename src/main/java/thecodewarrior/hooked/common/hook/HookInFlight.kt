package thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.saving.Savable
import net.minecraft.util.math.Vec3d
import java.util.*

@Savable
data class HookInFlight(
        /**
         * The position of the back of the hook
         */
        var pos: Vec3d,
        /**
         * The direction the hook is facing
         */
        var direction: Vec3d,
        /**
         * The UUID of the hook
         */
        val uuid: UUID
)
