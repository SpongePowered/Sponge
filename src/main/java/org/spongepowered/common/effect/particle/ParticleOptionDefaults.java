package org.spongepowered.common.effect.particle;

import com.google.common.collect.ImmutableMap;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.RedstoneParticleData;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Color;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;

public final class ParticleOptionDefaults {

    /**
     * Generates the default particle options for a given internal ParticleType (non-numerical particles).
     */
    public static ImmutableMap<ParticleOption<?>, Object> generateDefaultsForNamed(ParticleType<?> type) {
        final Map<ParticleOption<?>, Object> options = new HashMap<>();

        options.put(ParticleOptions.OFFSET.get(), Vector3d.ZERO);
        options.put(ParticleOptions.QUANTITY.get(), 1);

        if (type.getDeserializer() == BlockParticleData.DESERIALIZER) {
            options.put(ParticleOptions.BLOCK_STATE.get(), BlockTypes.AIR.get().getDefaultState());
        } else if (type.getDeserializer() == ItemParticleData.DESERIALIZER) {
            options.put(ParticleOptions.ITEM_STACK_SNAPSHOT.get(), ItemStackSnapshot.empty());
        } else if (type.getDeserializer() == RedstoneParticleData.DESERIALIZER) {
            options.put(ParticleOptions.COLOR.get(), Color.RED);
        }

        return ImmutableMap.copyOf(options);
    }
}