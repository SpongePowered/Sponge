package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Optional;

@Mixin(net.minecraft.stats.Achievement.class)
@Implements(@Interface(iface = Achievement.class, prefix = "achievement$"))
public abstract class MixinAchievement extends StatBase implements Achievement {

    @Shadow public Achievement parentAchievement;

    public MixinAchievement(String statIdIn, IChatComponent statNameIn, IStatType typeIn) {
        super(statIdIn, statNameIn, typeIn);
    }

    public Translation achievement$getDescription() {
        return new SpongeTranslation("achievement." + this.statId + ".desc");
    }

    public Optional<Achievement> achievement$getParent() {
        return Optional.ofNullable(this.parentAchievement);
    }

    public Collection<Achievement> achievement$getChildren() {
        return null;
    }

    public Optional<Statistic> achievement$getSourceStatistic() {
        return Optional.of((Statistic) this);
    }

    public Optional<Long> achievement$getStatisticTargetValue() {
        return Optional.empty();
    }

    public String achievement$getId() {
        return "achievement." + this.statId;
    }

    public String achievement$getName() {
        return this.statId;
    }

    public Text achievement$toText() {
        return Texts.of(achievement$getTranslation());
    }

    public Translation achievement$getTranslation() {
        return new SpongeTranslation("achievement." + this.statId);
    }
}
