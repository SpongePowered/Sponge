package org.spongepowered.common.mixin.inventory.api.inventory.type;

import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.inventory.custom.ViewableCustomInventory;

@Mixin(ViewableCustomInventory.class)
public abstract class ViewableCustomInventoryMixin_API implements ViewableInventory.Custom {

}
