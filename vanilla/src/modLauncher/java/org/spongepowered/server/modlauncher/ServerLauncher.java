package org.spongepowered.server.modlauncher;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.common.launch.Launcher;

import java.nio.file.Path;
import java.util.List;

public final class ServerLauncher extends Launcher {

    public static void launch(final String pluginSpiVersion, final Path baseDirectory, final List<Path> pluginDirectories, final String[] args) {
        Launcher.populateBlackboard(pluginSpiVersion, baseDirectory, pluginDirectories);
        Launcher.loadPlugins();
        Launcher.getLogger().info("Loading Minecraft Server, please wait...");
        MinecraftServer.main(args);
    }
}
