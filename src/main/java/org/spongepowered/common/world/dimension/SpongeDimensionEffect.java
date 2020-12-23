package org.spongepowered.common.world.dimension;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.dimension.DimensionEffect;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

public final class SpongeDimensionEffect extends AbstractResourceKeyed implements DimensionEffect {

    public SpongeDimensionEffect(final ResourceKey key) {
        super(key);
    }

    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<DimensionEffect, DimensionEffect.Builder> implements DimensionEffect.Builder {

        @Override
        protected DimensionEffect build0() {
            return new SpongeDimensionEffect(this.key);
        }
    }

    public static final class FactoryImpl implements DimensionEffect.Factory {

        @Override
        public DimensionEffect overworld() {
            return SpongeDimensionEffects.OVERWORLD;
        }

        @Override
        public DimensionEffect nether() {
            return SpongeDimensionEffects.NETHER;
        }

        @Override
        public DimensionEffect end() {
            return SpongeDimensionEffects.END;
        }
    }
}
