package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.extra.fluid.SpongeFluidStackBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.mixin.accessor.item.BucketItemAccessor;

import java.util.Optional;

// TODO - setter not possible - the API needs to be refactored, as it's no longer possible to change the type of an ItemStack
public class ItemStackFluidProvider extends ItemStackDataProvider<FluidStackSnapshot> {

    private static final FluidStackSnapshot WATER = new SpongeFluidStackBuilder().fluid(FluidTypes.WATER).volume(1000).build().createSnapshot();
    private static final FluidStackSnapshot LAVA = new SpongeFluidStackBuilder().fluid(FluidTypes.LAVA).volume(1000).build().createSnapshot();

    public ItemStackFluidProvider() {
        super(Keys.FLUID_ITEM_STACK);
    }

    @Override
    protected Optional<FluidStackSnapshot> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() instanceof BucketItem) {
            Fluid fluid = ((BucketItemAccessor) dataHolder.getItem()).accessor$getContainedBlock();
            if (fluid == Fluids.EMPTY) {
                return Optional.empty();
            }
            return Optional.of(new SpongeFluidStackSnapshotBuilder().fluid((FluidType) fluid).volume(1000).build());
        }
        return Optional.empty();
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof BucketItem;
    }
}
