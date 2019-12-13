package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.BoundedValue;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public abstract class BlockStateBoundedDataProvider<E> extends BlockStateDataProviderBase<BoundedValue<E>, E> {

    public BlockStateBoundedDataProvider(Key<? extends BoundedValue<E>> key, Class<? extends Block> blockType) {
        super((Key<BoundedValue<E>>) key, blockType);
    }

    public BlockStateBoundedDataProvider(Key<? extends BoundedValue<E>> key, Predicate<Block> blockPredicate) {
        super((Key<BoundedValue<E>>) key, blockPredicate);
    }
}
