package org.spongepowered.common.launch.plugin;

import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.plugin.PluginContainer;

public interface SpongePluginManager extends PluginManager {

    void addPlugin(PluginContainer plugin);
}
