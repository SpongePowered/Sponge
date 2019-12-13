package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.function.Predicate;

public abstract class BlockStateDataProviderBase<V extends Value<E>, E> extends GenericImmutableDataProviderBase<BlockState, V, E> {

    private final Predicate<Block> blockPredicate;

    BlockStateDataProviderBase(Key<V> key, Class<? extends Block> blockType) {
        this(key, new ConstantBlockPredicate(blockType));
    }

    BlockStateDataProviderBase(Key<V> key, Predicate<Block> blockPredicate) {
        super(key);
        this.blockPredicate = blockPredicate;
    }

    public Predicate<Block> getBlockPredicate() {
        return this.blockPredicate;
    }

    @Override
    protected boolean supports(BlockState dataHolder) {
        return this.blockPredicate.test(dataHolder.getBlock());
    }
}
