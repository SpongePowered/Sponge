package org.spongepowered.common.mixin.api.item.inventory;

import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.SingleBlockCarrier;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.tileentity.DefaultSingleBlockCarrier;

@Mixin({
    ContainerRepair.class,
    ContainerEnchantment.class,
    ContainerWorkbench.class
})
public abstract class SingleBlockCarrierMixin_API implements SingleBlockCarrier {

    @Override
    public Inventory getInventory(Direction from) {
        return DefaultSingleBlockCarrier.getInventory(from, this);
    }

}
