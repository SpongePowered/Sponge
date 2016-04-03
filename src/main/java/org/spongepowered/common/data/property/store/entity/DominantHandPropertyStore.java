package org.spongepowered.common.data.property.store.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.api.data.property.entity.DominantHandProperty;
import org.spongepowered.api.data.type.HandSide;
import org.spongepowered.common.data.property.store.common.AbstractEntityPropertyStore;

import java.util.Optional;

public class DominantHandPropertyStore extends AbstractEntityPropertyStore<DominantHandProperty> {

    @Override
    protected Optional<DominantHandProperty> getForEntity(Entity entity) {
        if(!(entity instanceof EntityPlayer)) return Optional.empty();

        EnumHandSide hand = ((EntityPlayer) entity).getPrimaryHand();
        HandSide type = (HandSide) (Object) hand;
        return Optional.of(new DominantHandProperty(type));
    }
}
