package org.spongepowered.common.interfaces.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

public interface IMixinWorldProvider {

    void setGeneratorSettings(String generatorSettings);

    int getRespawnDimension(EntityPlayerMP entityPlayerMP);

    BlockPos getRandomizedSpawnPoint();
}
