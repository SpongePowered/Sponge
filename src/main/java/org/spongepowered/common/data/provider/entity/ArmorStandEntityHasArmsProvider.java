package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;

import java.util.Optional;

public class ArmorStandEntityHasArmsProvider extends GenericMutableDataProvider<ArmorStandEntity, Boolean> {

    public ArmorStandEntityHasArmsProvider() {
        super(Keys.ARMOR_STAND_HAS_ARMS);
    }

    @Override
    protected Optional<Boolean> getFrom(ArmorStandEntity dataHolder) {
        return Optional.of(dataHolder.getShowArms());
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Boolean value) {
        ((ArmorStandEntityAccessor) dataHolder).accessor$setShowArms(value);
        return true;
    }
}
