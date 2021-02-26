package dev.thecodewarrior.hooked.client.glitter

import com.teamwizardry.librarianlib.core.util.loc
import java.awt.Color
import com.teamwizardry.librarianlib.glitter.ParticleSystem
import com.teamwizardry.librarianlib.glitter.bindings.ConstantBinding
import com.teamwizardry.librarianlib.glitter.modules.BasicPhysicsUpdateModule
import com.teamwizardry.librarianlib.glitter.modules.SetValueUpdateModule
import com.teamwizardry.librarianlib.glitter.modules.SpriteRenderModule
import com.teamwizardry.librarianlib.glitter.modules.SpriteRenderOptions
import com.teamwizardry.librarianlib.math.Easing

object EnderHookParticleSystem: ParticleSystem() {
    val defaultColor: Color = Color(0.9f, 0.3f, 1.0f, 1.0f)

    override fun configure() {
        val position = bind(3)
        val previousPosition = bind(3)
        val velocity = bind(3)

        val startDirection = bind(3)
        val rotationAxis = bind(3)
        val angleStart = bind(1)
        val angleEnd = bind(1)

        val size = bind(2)
        val color = bind(4)

        val direction = bind(3)
        val previousDirection = bind(3)

        val rotatedDirection = RotationBinding(
            lifetime = lifetime,
            age = age,
            easing = Easing.easeOutQuad,
            inputVector = startDirection,
            rotationAxis = rotationAxis,
            angleStart = angleStart,
            angleEnd = angleEnd
        )

        updateModules.add(SetValueUpdateModule(previousDirection, direction))
        updateModules.add(SetValueUpdateModule(direction, rotatedDirection))

        updateModules.add(
            BasicPhysicsUpdateModule(
                position = position,
                previousPosition = previousPosition,
                velocity = velocity,
                enableCollision = false,
                gravity = ConstantBinding(0.0),
                damping = ConstantBinding(0.05)
            )
        )

        renderModules.add(
            SpriteRenderModule.build(
                SpriteRenderOptions.build(loc("hooked:textures/glitter/bar.png"))
                    .worldLight(true)
                    .diffuseLight(true)
                    .build(),
                position
            )
                .previousPosition(previousPosition)
                .color(color)
                .size(size)
                .upVector(PartialTickBinding(previousDirection, direction))
                .build()
        )

//        renderModules.add(
//            VelocityRenderModule(
//                blend = true,
//                previousPosition = position,
//                position = position,
//                velocity = direction,
//                color = ConstantBinding(1.0, 0.0, 0.0, 1.0),
//                size = 2f,
//                alpha = null
//            )
//        )
    }

    fun spawn(
        lifetime: Int,
        positionX: Double, positionY: Double, positionZ: Double,
        velocityX: Double, velocityY: Double, velocityZ: Double,
        directionX: Double, directionY: Double, directionZ: Double,
        rotationAxisX: Double, rotationAxisY: Double, rotationAxisZ: Double,
        angleStart: Double, angleEnd: Double,
        length: Double,
        red: Double, green: Double, blue: Double, alpha: Double,
    ) {
        addParticle(
            lifetime,

            positionX, positionY, positionZ, // position = bind(3)
            positionX, positionY, positionZ, // previousPosition = bind(3)
            velocityX, velocityY, velocityZ, // velocity = bind(3)

            directionX, directionY, directionZ, // startDirection = bind(3)
            rotationAxisX, rotationAxisY, rotationAxisZ, // rotationAxis = bind(3)
            angleStart, // angleStart = bind(1)
            angleEnd, // angleEnd = bind(1)

            1/16.0, length, // size = bind(2)
            red, green, blue, alpha, // color = bind(4)

            directionX, directionY, directionZ, // direction = bind(3)
            directionX, directionY, directionZ, // previousDirection = bind(3)
        )
    }
}