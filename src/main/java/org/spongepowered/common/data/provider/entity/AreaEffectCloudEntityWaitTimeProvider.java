package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.AreaEffectCloudEntityAccessor;

import java.util.Optional;

public class AreaEffectCloudEntityWaitTimeProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, Integer> {

    public AreaEffectCloudEntityWaitTimeProvider() {
        super(Keys.AREA_EFFECT_CLOUD_WAIT_TIME);
    }

    @Override
    protected Optional<Integer> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of(((AreaEffectCloudEntityAccessor) dataHolder).accessor$getWaitTime());
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, Integer value) {
        dataHolder.setWaitTime(value);
        return true;
    }
}
