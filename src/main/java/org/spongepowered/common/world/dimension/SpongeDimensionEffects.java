package org.spongepowered.common.world.dimension;

import org.spongepowered.api.ResourceKey;

public final class SpongeDimensionEffects {

    public static final SpongeDimensionEffect OVERWORLD = new SpongeDimensionEffect(ResourceKey.minecraft("overworld"));

    public static final SpongeDimensionEffect NETHER = new SpongeDimensionEffect(ResourceKey.minecraft("nether"));

    public static final SpongeDimensionEffect END = new SpongeDimensionEffect(ResourceKey.minecraft("end"));

    private SpongeDimensionEffects() {
    }
}
