package org.spongepowered.forge.applaunch.plugin;

import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.api.IEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ForgeAppPluginPlatform implements PluginPlatform {

    private final Environment environment;
    private final Logger logger;
    private final List<Path> pluginDirectories;

    public ForgeAppPluginPlatform(final Environment environment) {
        this.environment = environment;
        this.logger = LogManager.getLogger("App Launch");
        this.pluginDirectories = new ArrayList<>();

        this.pluginDirectories.add(FMLPaths.MODSDIR.get());
        // TODO We do not have all the arguments passed to Forge from CLI...How do we correctly handle knowing the plugins directory??
    }

    @Override
    public String version() {
        return this.environment.getProperty(IEnvironment.Keys.VERSION.get()).orElse("dev");
    }

    @Override
    public void setVersion(final String version) {
        // NOOP
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path baseDirectory() {
        return this.environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Paths.get("."));
    }

    @Override
    public void setBaseDirectory(final Path baseDirectory) {
        // NOOP
    }

    @Override
    public List<Path> pluginDirectories() {
        return this.pluginDirectories;
    }

    @Override
    public void setPluginDirectories(final List<Path> pluginDirectories) {
        // NOOP
    }
}
