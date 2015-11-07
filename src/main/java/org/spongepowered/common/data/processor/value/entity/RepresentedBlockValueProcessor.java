package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class RepresentedBlockValueProcessor extends AbstractSpongeValueProcessor<EntityMinecart, BlockState, Value<BlockState>> {

    public RepresentedBlockValueProcessor() {
        super(EntityMinecart.class, Keys.REPRESENTED_BLOCK);
    }

    @Override
    protected Value<BlockState> constructValue(BlockState value) {
        return new SpongeValue<>(Keys.REPRESENTED_BLOCK, (BlockState) Blocks.air.getDefaultState(), value);
    }

    @Override
    protected boolean set(EntityMinecart container, BlockState value) {
        container.func_174899_a((IBlockState) value);
        return true;
    }

    @Override
    protected Optional<BlockState> getVal(EntityMinecart container) {
        if(!container.hasDisplayTile()) return Optional.empty();
        return Optional.of((BlockState) container.getDisplayTile());
    }

    @Override
    protected ImmutableValue<BlockState> constructImmutableValue(BlockState value) {
        return new ImmutableSpongeValue<>(Keys.REPRESENTED_BLOCK, (BlockState) Blocks.air.getDefaultState(), value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if(container instanceof EntityMinecart) {
            EntityMinecart cart = (EntityMinecart) container;
            ImmutableValue<BlockState> block = new ImmutableSpongeValue<>(Keys.REPRESENTED_BLOCK, (BlockState) cart.getDisplayTile());
            cart.setHasDisplayTile(false);
            return DataTransactionBuilder.builder().replace(block).build();
        }
        return DataTransactionBuilder.failNoData();
    }
}
