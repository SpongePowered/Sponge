package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.monster.ZombieEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class ZombieEntityIsAdultProvider extends GenericMutableDataProvider<ZombieEntity, Boolean> {

    public ZombieEntityIsAdultProvider() {
        super(Keys.IS_ADULT);
    }

    @Override
    protected Optional<Boolean> getFrom(ZombieEntity dataHolder) {
        return OptBool.of(!dataHolder.isChild());
    }

    @Override
    protected boolean set(ZombieEntity dataHolder, Boolean value) {
        dataHolder.setChild(!value);
        return true;
    }
}
