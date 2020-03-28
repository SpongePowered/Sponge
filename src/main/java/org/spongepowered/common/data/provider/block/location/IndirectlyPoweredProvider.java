package org.spongepowered.common.data.provider.block.location;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class IndirectlyPoweredProvider extends GenericMutableDataProvider<Location, Boolean> {

    public IndirectlyPoweredProvider() {
        super(Keys.IS_INDIRECTLY_POWERED);
    }

    @Override
    protected Optional<Boolean> getFrom(Location dataHolder) {
        World world = (World) dataHolder.getWorld();
        BlockPos pos = VecHelper.toBlockPos(dataHolder);
        return Optional.of(world.getRedstonePowerFromNeighbors(pos) > 0);
    }
}
