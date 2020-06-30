package org.spongepowered.vanilla.modlauncher;

import org.spongepowered.common.launch.Launcher;
import org.spongepowered.common.launch.plugin.DummyPluginContainer;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.util.PluginMetadataHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public abstract class VanillaLauncher extends Launcher {

    protected VanillaLauncher(SpongePluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    protected void createPlatformPlugins(final Path gameDirectory) {
        try {
            final Collection<PluginMetadata> read = PluginMetadataHelper.builder().build().read(Launcher.class.getResourceAsStream("META-INF/plugins.json"));
            for (final PluginMetadata metadata : read) {
                this.getPluginManager().addPlugin(new DummyPluginContainer(metadata, gameDirectory, this.getLogger(), this));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load metadata information for the implementation! This should be impossible!");
        }
    }
}
