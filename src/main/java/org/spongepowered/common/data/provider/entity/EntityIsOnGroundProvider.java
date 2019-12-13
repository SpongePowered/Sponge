package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class EntityIsOnGroundProvider extends GenericMutableDataProvider<Entity, Boolean> {

    public EntityIsOnGroundProvider() {
        super(Keys.IS_ON_GROUND);
    }

    @Override
    protected Optional<Boolean> getFrom(Entity dataHolder) {
        return Optional.of(dataHolder.onGround);
    }

    @Override
    protected boolean set(Entity dataHolder, Boolean value) {
        return false;
    }
}
