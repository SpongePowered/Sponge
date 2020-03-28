package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.mixin.accessor.item.ToolItemAccessor;

import java.util.Optional;

public class ItemStackEfficiencyProvider extends ItemStackDataProvider<Float> {

    public ItemStackEfficiencyProvider() {
        super(Keys.EFFICIENCY);
    }

    @Override
    protected Optional<Float> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() instanceof ToolItemAccessor) {
            float efficiency = ((ToolItemAccessor) dataHolder.getItem()).accessor$getEfficiency();
            return Optional.of(efficiency);
        }
        return Optional.empty();
    }
}
