package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.passive.PandaEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.entity.passive.PandaEntityAccessor;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class PandaEntityEatingTimeProvider extends GenericMutableDataProvider<PandaEntity, Integer> {

    public PandaEntityEatingTimeProvider() {
        super(Keys.EATING_TIME);
    }

    @Override
    protected Optional<Integer> getFrom(PandaEntity dataHolder) {
        return Optional.of(((PandaEntityAccessor) dataHolder).accessor$getEatingTime());
    }

    @Override
    protected boolean set(PandaEntity dataHolder, Integer value) {
        ((PandaEntityAccessor) dataHolder).accessor$setEatingTime(value);
        return true;
    }
}
