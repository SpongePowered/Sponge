package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class AreaEffectCloudEntityDurationProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, Integer> {

    public AreaEffectCloudEntityDurationProvider() {
        super(Keys.AREA_EFFECT_CLOUD_DURATION);
    }

    @Override
    protected Optional<Integer> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of(dataHolder.getDuration());
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, Integer value) {
        dataHolder.setDuration(value);
        return true;
    }
}
