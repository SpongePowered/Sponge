package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

@Mixin(StatBase.class)
@Implements(@Interface(iface = Statistic.class, prefix = "statistic$"))
public abstract class MixinStatBase implements Statistic {

    @Shadow public String statId;
    @Shadow private IStatType type;
    @Shadow private IScoreObjectiveCriteria field_150957_c;

    public Optional<StatisticFormat> statistic$getStatisticFormat() {
        return Optional.of((StatisticFormat) this.type);
    }

    public StatisticGroup statistic$getGroup() {
        return (StatisticGroup) this.field_150957_c;
    }

    public String statistic$getId() {
        return this.statId;
    }

    public String statistic$getName() {
        return this.statId;
    }

    public Translation statistic$getTranslation() {
        return new SpongeTranslation(this.statId);
    }
}
