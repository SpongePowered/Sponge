package org.spongepowered.common.data.provider.block.location;

import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class GroundLuminanceProvider extends GenericMutableDataProvider<Location, Integer> {

    public GroundLuminanceProvider() {
        super(Keys.GROUND_LUMINANCE);
    }

    @Override
    protected Optional<Integer> getFrom(Location dataHolder) {
        World world = (World) dataHolder.getWorld();
        int light = world.getLightFor(LightType.BLOCK, VecHelper.toBlockPos(dataHolder));
        return Optional.of(light);
    }
}
