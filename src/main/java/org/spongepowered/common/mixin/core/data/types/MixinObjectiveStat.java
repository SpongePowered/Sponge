package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.scoreboard.ScoreDummyCriteria;
import net.minecraft.stats.ObjectiveStat;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ObjectiveStat.class)
public abstract class MixinObjectiveStat extends ScoreDummyCriteria implements IMixinObjectiveStat {

    @Shadow private StatBase field_151459_g;

    public MixinObjectiveStat(String name) {
        super(name);
    }

    @Override
    public Statistic getStatistic() {
        return (Statistic) this.field_151459_g;
    }
}
