package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;

import java.util.function.Predicate;

public class ConstantBlockPredicate implements Predicate<Block> {

    private final Class<? extends Block> blockType;

    public ConstantBlockPredicate(Class<? extends Block> blockType) {
        this.blockType = blockType;
    }

    public Class<? extends Block> getBlockType() {
        return this.blockType;
    }

    @Override
    public boolean test(Block block) {
        return this.blockType.isInstance(block);
    }
}
