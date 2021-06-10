package org.spongepowered.common.bridge.world.level.biome;

import net.minecraft.world.level.biome.OverworldBiomeSource;
import org.spongepowered.common.world.biome.provider.OverworldBiomeSourceHelper;

public interface OverworldBiomeSourceBridge {

    OverworldBiomeSource bridge$decorateData(final OverworldBiomeSourceHelper.SpongeDataSection data);

    OverworldBiomeSourceHelper.SpongeDataSection bridge$createData();
}
