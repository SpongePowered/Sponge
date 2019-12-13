package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AgeableEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class AgeableEntityIsAdultProvider extends GenericMutableDataProvider<AgeableEntity, Boolean> {

    public AgeableEntityIsAdultProvider() {
        super(Keys.IS_ADULT);
    }

    @Override
    protected Optional<Boolean> getFrom(AgeableEntity dataHolder) {
        return OptBool.of(!dataHolder.isChild());
    }

    @Override
    protected boolean set(AgeableEntity dataHolder, Boolean value) {
        dataHolder.setGrowingAge(value ? Constants.Entity.Ageable.ADULT : Constants.Entity.Ageable.CHILD);
        return true;
    }
}
