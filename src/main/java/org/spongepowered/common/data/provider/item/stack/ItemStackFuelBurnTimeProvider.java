package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackFuelBurnTimeProvider extends ItemStackDataProvider<Integer> {

    public ItemStackFuelBurnTimeProvider() {
        super(Keys.BURN_TIME);
    }

    @Override
    protected Optional<Integer> getFrom(ItemStack dataHolder) {
        final Integer burnTime = AbstractFurnaceTileEntity.getBurnTimes().get(dataHolder.getItem());
        if (burnTime != null && burnTime > 0) {
            return Optional.of(burnTime);
        }
        return Optional.empty();
    }
}
