package org.spongepowered.common.mixin.core.scoreboard;

import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.scoreboard.TeamBridge;
import org.spongepowered.common.text.format.SpongeTextColor;

@Mixin(Team.class)
public abstract class TeamMixin implements TeamBridge {

    @Shadow public abstract TextFormatting getColor();

    @Override
    public TextColor bridge$getColor() {
        return SpongeTextColor.of(this.getColor());
    }
}
