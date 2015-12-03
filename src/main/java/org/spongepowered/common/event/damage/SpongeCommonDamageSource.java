package org.spongepowered.common.event.damage;

import net.minecraft.util.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.world.difficulty.Difficulty;

/*
To summarize, the way this works is that DamageSource isn't directly created, but
rather that any API abstract classes are automatically set up to extend
this class at runtime. All method calls are forwarded to the proper API calls
by definition. Granted, this cannot actively implement the interface,
but it can certainly declare the methods as abstract.

More notes are geared for abstraction of generating the builders, since those
will require sending the builders into the ctors.
 */
public abstract class SpongeCommonDamageSource extends DamageSource implements org.spongepowered.api.event.cause.entity.damage.source.DamageSource {

    protected SpongeCommonDamageSource() {
        super("SpongeDamageSource");
        this.damageType = getType().getId();
    }

    @Override
    public boolean isExplosion() {
        return this.isExplosive();
    }

    @Override
    public boolean isUnblockable() {
        return this.isBypassingArmor();
    }

    @Override
    public boolean canHarmInCreative() {
        return this.doesAffectCreative();
    }

    @Override
    public boolean isDamageAbsolute() {
        return this.isAbsolute();
    }

    @Override
    public boolean isDifficultyScaled() {
        return this.isScaledByDifficulty();
    }

    @Override
    public boolean isMagicDamage() {
        return this.isMagic();
    }

}
