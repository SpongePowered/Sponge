package org.spongepowered.common.world.generation.settings.flat;

import net.minecraft.block.Block;
import net.minecraft.world.gen.FlatLayerInfo;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.generation.settings.flat.LayerSettings;
import org.spongepowered.common.accessor.world.gen.FlatLayerInfoAccessor;

public final class SpongeLayerSettingsFactory implements LayerSettings.Factory {

    @Override
    public LayerSettings of(final int height, final BlockState block) {
        final FlatLayerInfo layer = new FlatLayerInfo(height, (Block) block.getType());
        ((FlatLayerInfoAccessor) layer).accessor$blockState((net.minecraft.block.BlockState) block);
        return (LayerSettings) layer;
    }
}
