package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AgeableEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class AgeableEntityAgeProvider extends GenericMutableDataProvider<AgeableEntity, Integer> {

    public AgeableEntityAgeProvider() {
        super(Keys.AGEABLE_AGE);
    }

    @Override
    protected Optional<Integer> getFrom(AgeableEntity dataHolder) {
        return Optional.of(dataHolder.getGrowingAge());
    }

    @Override
    protected boolean set(AgeableEntity dataHolder, Integer value) {
        dataHolder.setGrowingAge(value);
        return true;
    }
}
