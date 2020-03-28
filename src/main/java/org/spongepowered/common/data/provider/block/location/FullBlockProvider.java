package org.spongepowered.common.data.provider.block.location;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class FullBlockProvider extends GenericMutableDataProvider<Location, Boolean> {

    public FullBlockProvider() {
        super(Keys.FULL_BLOCK);
    }

    @Override
    protected Optional<Boolean> getFrom(Location dataHolder) {
        BlockState block = (BlockState) dataHolder.getBlock();
        World world = (World) dataHolder.getWorld();
        BlockPos pos = VecHelper.toBlockPos(dataHolder.getPosition());
        return Optional.of(block.isOpaqueCube(world, pos));
    }
}
