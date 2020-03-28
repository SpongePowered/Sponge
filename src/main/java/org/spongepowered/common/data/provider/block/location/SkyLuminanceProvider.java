package org.spongepowered.common.data.provider.block.location;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class SkyLuminanceProvider extends GenericMutableDataProvider<Location, Integer> {

    public SkyLuminanceProvider() {
        super(Keys.SKY_LIGHT);
    }

    @Override
    protected Optional<Integer> getFrom(Location dataHolder) {
        World world = (World) dataHolder.getWorld();
        BlockPos pos = VecHelper.toBlockPos(dataHolder);
        int lightFor = world.getLightFor(LightType.SKY, pos);
        return Optional.of(lightFor);
    }
}
