package dev.thecodewarrior.hooked;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class MixinConnector implements IMixinConnector {
    @Override
    public void connect() {
        Mixins.addConfiguration("META-INF/dev.thecodewarrior.hooked-mixins.json");
    }
}
