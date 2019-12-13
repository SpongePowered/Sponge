package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;

import java.util.Optional;

public class ArmorStandEntityHasMarkerProvider extends GenericMutableDataProvider<ArmorStandEntity, Boolean> {

    public ArmorStandEntityHasMarkerProvider() {
        super(Keys.ARMOR_STAND_HAS_MARKER);
    }

    @Override
    protected Optional<Boolean> getFrom(ArmorStandEntity dataHolder) {
        return Optional.of(dataHolder.hasMarker());
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Boolean value) {
        ((ArmorStandEntityAccessor) dataHolder).accessor$setMarker(value);
        return true;
    }
}
