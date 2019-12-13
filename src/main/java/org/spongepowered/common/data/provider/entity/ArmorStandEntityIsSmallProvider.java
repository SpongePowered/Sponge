package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;

import java.util.Optional;

public class ArmorStandEntityIsSmallProvider extends GenericMutableDataProvider<ArmorStandEntity, Boolean> {

    public ArmorStandEntityIsSmallProvider() {
        super(Keys.ARMOR_STAND_IS_SMALL);
    }

    @Override
    protected Optional<Boolean> getFrom(ArmorStandEntity dataHolder) {
        return Optional.of(dataHolder.isSmall());
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Boolean value) {
        ((ArmorStandEntityAccessor) dataHolder).accessor$setSmall(value);
        return true;
    }
}
