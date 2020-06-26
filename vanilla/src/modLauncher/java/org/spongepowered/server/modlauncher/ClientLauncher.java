package org.spongepowered.server.modlauncher;

import net.minecraft.client.main.Main;
import org.spongepowered.common.launch.Launcher;

import java.nio.file.Path;
import java.util.List;

public final class ClientLauncher extends Launcher {

    public static void launch(final String pluginSpiVersion, final Path baseDirectory, final List<Path> pluginDirectories, final String[] args) {
        Launcher.populateBlackboard(pluginSpiVersion, baseDirectory, pluginDirectories);
        Launcher.loadPlugins();
        Launcher.getLogger().info("Loading Minecraft Client, please wait...");
        Main.main(args);
    }
}