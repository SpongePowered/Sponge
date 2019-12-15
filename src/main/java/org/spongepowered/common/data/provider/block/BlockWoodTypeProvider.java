package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.common.data.provider.GenericImmutableDataProvider;

import java.util.Optional;

public class BlockWoodTypeProvider extends GenericImmutableDataProvider<Block, WoodType> {

    public BlockWoodTypeProvider() {
        super(Keys.WOOD_TYPE);
    }

    @Override
    protected Optional<WoodType> getFrom(Block dataHolder) {
        // TODO
        return Optional.empty();
    }

    @Override
    protected Optional<Block> set(Block dataHolder, WoodType value) {
        // TODO
        return Optional.empty();
    }
}
