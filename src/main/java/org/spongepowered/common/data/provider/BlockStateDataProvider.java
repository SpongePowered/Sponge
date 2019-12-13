package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public abstract class BlockStateDataProvider<E> extends BlockStateDataProviderBase<Value<E>, E> {

    public BlockStateDataProvider(Key<? extends Value<E>> key, Class<? extends Block> blockType) {
        super((Key<Value<E>>) key, blockType);
    }

    public BlockStateDataProvider(Key<? extends Value<E>> key, Predicate<Block> blockPredicate) {
        super((Key<Value<E>>) key, blockPredicate);
    }
}
