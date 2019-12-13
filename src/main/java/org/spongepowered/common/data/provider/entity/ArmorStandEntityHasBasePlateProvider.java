package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;

import java.util.Optional;

public class ArmorStandEntityHasBasePlateProvider extends GenericMutableDataProvider<ArmorStandEntity, Boolean> {

    public ArmorStandEntityHasBasePlateProvider() {
        super(Keys.ARMOR_STAND_HAS_ARMS);
    }

    @Override
    protected Optional<Boolean> getFrom(ArmorStandEntity dataHolder) {
        return Optional.of(!dataHolder.hasNoBasePlate());
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Boolean value) {
        ((ArmorStandEntityAccessor) dataHolder).accessor$setNoBasePlate(!value);
        return true;
    }
}
