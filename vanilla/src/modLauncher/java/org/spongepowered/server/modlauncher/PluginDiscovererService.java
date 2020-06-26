package org.spongepowered.server.modlauncher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.spongepowered.common.launch.plugin.PluginLoader;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginFile;
import org.spongepowered.plugin.PluginKeys;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public final class PluginDiscovererService implements ITransformationService {

    private static final String NAME = "plugin_discoverer";
    private final PluginEnvironment pluginEnvironment;

    private PluginLoader pluginLoader;

    public PluginDiscovererService() {
        this.pluginEnvironment = Main.getLaunchPluginEnvironment();
    }

    @Nonnull
    @Override
    public String name() {
        return PluginDiscovererService.NAME;
    }

    @Override
    public void initialize(final IEnvironment environment) {
        this.pluginLoader.initialize();
    }

    @Override
    public void beginScanning(final IEnvironment environment) {
        //NOOP
    }

    @Override
    public List<Map.Entry<String, Path>> runScan(final IEnvironment environment) {
        this.pluginLoader.discoverResources();

        final List<Map.Entry<String, Path>> launchResources = new ArrayList<>();

        for (final Map.Entry<String, Collection<PluginFile>> resourcesEntry : this.pluginLoader.getResources().entrySet()) {
            final Collection<PluginFile> resources = resourcesEntry.getValue();
            launchResources.addAll(
                resources
                    .stream()
                    .map(pluginFile -> Maps.immutableEntry(pluginFile.getRootPath().getFileName().toString(), pluginFile.getRootPath()))
                    .collect(Collectors.toList())
            );
        }

        return launchResources;
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        this.pluginEnvironment.getLogger().info("SpongePowered PLUGIN Subsystem Version={} Service=ModLauncher", this.pluginEnvironment.getBlackboard().get(PluginKeys.VERSION).get());
        this.pluginLoader = new PluginLoader(this.pluginEnvironment);
        this.pluginLoader.discoverServices();
        this.pluginLoader.getServices().forEach((k, v) -> this.pluginLoader.getEnvironment().getLogger().info("Plugin language loader '{}' found.", k));
    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return ImmutableList.of();
    }
}
