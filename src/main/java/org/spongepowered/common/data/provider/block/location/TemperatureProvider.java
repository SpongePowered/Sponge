package org.spongepowered.common.data.provider.block.location;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class TemperatureProvider extends GenericMutableDataProvider<Location, Double> {

    public TemperatureProvider() {
        super(Keys.TEMPERATURE);
    }

    @Override
    protected Optional<Double> getFrom(Location dataHolder) {
        World world = (World) dataHolder.getWorld();
        BlockPos pos = VecHelper.toBlockPos(dataHolder);
        Biome biome = world.getBiome(pos);
        double temperature = biome.func_225486_c(pos);
        return Optional.of(temperature);
    }
}
