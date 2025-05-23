package dev.thecodewarrior.hooked.client.glitter

import com.teamwizardry.librarianlib.glitter.ReadParticleBinding
import com.teamwizardry.librarianlib.glitter.bindings.AbstractTimeBinding
import com.teamwizardry.librarianlib.math.Easing
import kotlin.math.cos
import kotlin.math.sin

class RotationBinding(
    lifetime: ReadParticleBinding,
    age: ReadParticleBinding,
    timescale: ReadParticleBinding? = null,
    offset: ReadParticleBinding? = null,
    easing: Easing,
    val inputVector: ReadParticleBinding,
    val rotationAxis: ReadParticleBinding,
    val angleStart: ReadParticleBinding,
    val angleEnd: ReadParticleBinding
): AbstractTimeBinding(lifetime, age, timescale, offset, easing) {
    override val contents: DoubleArray = DoubleArray(3)

    init {
        inputVector.require(3)
        rotationAxis.require(3)
        angleStart.require(1)
        angleEnd.require(1)
    }

    override fun load(particle: DoubleArray) {
        super.load(particle)
        inputVector.load(particle)
        val inputX = inputVector.contents[0]
        val inputY = inputVector.contents[1]
        val inputZ = inputVector.contents[2]
        rotationAxis.load(particle)
        val axisX = rotationAxis.contents[0]
        val axisY = rotationAxis.contents[1]
        val axisZ = rotationAxis.contents[2]
        angleStart.load(particle)
        val angleStart = angleStart.contents[0]
        angleEnd.load(particle)
        val angleEnd = angleEnd.contents[0]

        val angle = angleStart + (angleEnd - angleStart) * easing.ease(time.toFloat())

        // rotation around an axis:
        // out = axis * (axis dot input) + cos(angle)(axis cross input) cross axis + sin(angle)(axis cross input)

        // cross product formula reference: c = a x b
        // cX = aY * bZ - aZ * bY
        // cY = aZ * bX - aX * bZ
        // cZ = aX * bY - aY * bX

        val axisDotInput = axisX * inputX + axisY * inputY + axisZ * inputZ

        val axisCrossInputX = axisY * inputZ - axisZ * inputY
        val axisCrossInputY = axisZ * inputX - axisX * inputZ
        val axisCrossInputZ = axisX * inputY - axisY * inputX

        val axisCrossInputCrossAxisX = axisCrossInputY * axisZ - axisCrossInputZ * axisY
        val axisCrossInputCrossAxisY = axisCrossInputZ * axisX - axisCrossInputX * axisZ
        val axisCrossInputCrossAxisZ = axisCrossInputX * axisY - axisCrossInputY * axisX

        val sin = sin(angle)
        val cos = cos(angle)

        val transformedX = axisX * axisDotInput + cos * axisCrossInputCrossAxisX + sin * axisCrossInputX
        val transformedY = axisY * axisDotInput + cos * axisCrossInputCrossAxisY + sin * axisCrossInputY
        val transformedZ = axisZ * axisDotInput + cos * axisCrossInputCrossAxisZ + sin * axisCrossInputZ

        contents[0] = transformedX
        contents[1] = transformedY
        contents[2] = transformedZ
    }
}