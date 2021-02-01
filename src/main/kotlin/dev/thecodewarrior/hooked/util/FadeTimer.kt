package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.math.clamp

class FadeTimer {
    val value: Double
        get() = if(duration == 0) 0.0 else (countdown.toDouble() / duration).clamp(0.0, 1.0)

    private var countdown: Int = 0
    private var duration: Int = 0

    fun tick() {
        if(countdown > 0) {
            countdown--
        }
    }

    fun start(duration: Int) {
        if(duration < countdown)
            return // don't shorten the fade
        this.countdown = duration
        this.duration = duration
    }

    fun reset() {
        this.countdown = 0
        this.duration = 0
    }
}
