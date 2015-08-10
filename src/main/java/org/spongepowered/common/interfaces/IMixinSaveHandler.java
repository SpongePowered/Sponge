package org.spongepowered.common.interfaces;

import net.minecraft.world.storage.WorldInfo;

import java.io.IOException;

public interface IMixinSaveHandler {
    void loadSpongeDatData(WorldInfo info) throws IOException;
}
