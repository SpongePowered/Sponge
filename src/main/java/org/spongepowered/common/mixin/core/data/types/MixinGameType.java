package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.world.WorldSettings;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(WorldSettings.GameType.class)
@Implements(@Interface(iface = GameMode.class, prefix = "gamemode$"))
public class MixinGameType {
    @Shadow
    String name;

    public String gamemode$getId() {
        return this.name.toUpperCase();
    }

    public String gamemode$getName() {
        return this.name.toUpperCase();
    }

    public Translation gamemode$getTranslation() {
        return new SpongeTranslation("gameMode." + this.name.toLowerCase());
    }

    @Overwrite
    public String getName() {
        return this.name;
    }
}
