package org.spongepowered.server.modlauncher.handler;

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.server.modlauncher.Main;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

public final class ServerDevLaunchHandler implements ILaunchHandlerService {

    private final Logger logger;

    public ServerDevLaunchHandler() {
        this.logger = LogManager.getLogger("Sponge");
    }

    @Override
    public String name() {
        return "sponge_server_dev";
    }

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        // Allow the entire classpath to be transformed...for now
        for (final URL url : Java9ClassLoaderUtil.getSystemClassPathURLs()) {
            try {
                builder.addTransformationPath(Paths.get(url.toURI()));
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Callable<Void> launchService(final String[] arguments, final ITransformingClassLoader launchClassLoader) {

        this.logger.info("Transitioning to Sponge launcher, please wait...");

        return () -> {
            final PluginEnvironment launchPluginEnvironment = Main.getLaunchPluginEnvironment();
            Class.forName("org.spongepowered.launch.ServerLauncher", true, launchClassLoader.getInstance()).getMethod("launch", String.class, Path.class, List.class, String[].class).invoke(null,
                launchPluginEnvironment.getBlackboard().get(PluginKeys.VERSION).orElse(null),
                launchPluginEnvironment.getBlackboard().get(PluginKeys.BASE_DIRECTORY).orElse(null),
                launchPluginEnvironment.getBlackboard().get(PluginKeys.PLUGIN_DIRECTORIES).orElse(null),
                arguments);
            return null;
        };
    }
}
