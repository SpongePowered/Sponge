package org.spongepowered.common.mixin.inventory.api.container;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerType.class)
public class ContainerTypeMixin_API implements org.spongepowered.api.item.inventory.ContainerType {

    @Override
    public CatalogKey getKey() {
        return (CatalogKey)(Object) Registry.MENU.getKey(((ContainerType) (Object) this));
    }
}
