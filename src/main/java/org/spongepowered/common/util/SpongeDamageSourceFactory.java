package org.spongepowered.common.util;

import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;

public final class SpongeDamageSourceFactory implements DamageSource.Factory {

    @Override
    public DamageSource drowning() {
        return (DamageSource) net.minecraft.util.DamageSource.DROWN;
    }

    @Override
    public DamageSource dryout() {
        return (DamageSource) net.minecraft.util.DamageSource.DRY_OUT;
    }

    @Override
    public DamageSource falling() {
        return (DamageSource) net.minecraft.util.DamageSource.FALL;
    }

    @Override
    public DamageSource fireTick() {
        return (DamageSource) net.minecraft.util.DamageSource.ON_FIRE;
    }

    @Override
    public DamageSource generic() {
        return (DamageSource) net.minecraft.util.DamageSource.GENERIC;
    }

    @Override
    public DamageSource magic() {
        return (DamageSource) net.minecraft.util.DamageSource.MAGIC;
    }

    @Override
    public DamageSource starvation() {
        return (DamageSource) net.minecraft.util.DamageSource.STARVE;
    }

    @Override
    public DamageSource voidSource() {
        return (DamageSource) net.minecraft.util.DamageSource.OUT_OF_WORLD;
    }

    @Override
    public DamageSource wither() {
        return (DamageSource) net.minecraft.util.DamageSource.WITHER;
    }
}
