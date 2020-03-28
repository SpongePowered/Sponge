package org.spongepowered.common.data.provider.item.stack;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.mixin.accessor.item.ToolItemAccessor;

import java.util.Optional;
import java.util.Set;

public class ItemStackHarvestingProvider extends ItemStackDataProvider<Set<BlockType>> {

    public ItemStackHarvestingProvider() {
        super(Keys.CAN_HARVEST);
    }

    @Override
    protected Optional<Set<BlockType>> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        if (item instanceof ToolItemAccessor && !(item instanceof PickaxeItem)) {
            Set<Block> set = ((ToolItemAccessor) item).accessor$getEffectiveBlocks();
            @SuppressWarnings("unchecked")
            ImmutableSet<BlockType> blocks = ImmutableSet.copyOf((Set) set);
            return Optional.of(blocks);
        }

        Set<BlockType> blocks = Registry.BLOCK.stream()
                .filter(block -> item.canHarvestBlock(block.getDefaultState()))
                .map(BlockType.class::cast)
                .collect(ImmutableSet.toImmutableSet());
        if (blocks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(blocks);
    }
}
