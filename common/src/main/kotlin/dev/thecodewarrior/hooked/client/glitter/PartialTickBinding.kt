package dev.thecodewarrior.hooked.client.glitter

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.glitter.ReadParticleBinding
import java.lang.IllegalArgumentException

class PartialTickBinding(val previousValue: ReadParticleBinding, val currentValue: ReadParticleBinding): ReadParticleBinding {
    init {
        if(previousValue.contents.size != currentValue.contents.size)
            throw IllegalArgumentException()
    }

    private val size = previousValue.contents.size
    override val contents: DoubleArray = DoubleArray(size)

    override fun load(particle: DoubleArray) {
        previousValue.load(particle)
        currentValue.load(particle)
        for(i in 0 until size) {
            contents[i] = Client.worldTime.interp(previousValue.contents[i], currentValue.contents[i])
        }
    }
}