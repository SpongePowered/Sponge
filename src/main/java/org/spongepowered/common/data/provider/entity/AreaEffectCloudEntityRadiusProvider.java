package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class AreaEffectCloudEntityRadiusProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, Double> {

    public AreaEffectCloudEntityRadiusProvider() {
        super(Keys.AREA_EFFECT_CLOUD_RADIUS);
    }

    @Override
    protected Optional<Double> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of((double) dataHolder.getRadius());
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, Double value) {
        dataHolder.setRadius(value.floatValue());
        return true;
    }
}
