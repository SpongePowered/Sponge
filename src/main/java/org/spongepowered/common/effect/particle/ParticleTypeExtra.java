package org.spongepowered.common.effect.particle;

import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleType;

import java.util.Map;

public interface ParticleTypeExtra extends ParticleType {

    Map<ParticleOption<?>, Object> getDefaultOptionMap();
}