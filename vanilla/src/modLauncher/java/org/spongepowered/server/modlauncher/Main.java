package org.spongepowered.server.modlauncher;

import cpw.mods.modlauncher.Launcher;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.spongepowered.server.modlauncher.util.ArgumentList;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public final class Main {

    private static PluginEnvironment pluginEnvironment;

    public static void main(final String[] args) {
        final OptionParser parser = new OptionParser();
        final ArgumentAcceptingOptionSpec<Path> gameDir = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().withValuesConvertedBy(new PathConverter(PathProperties.DIRECTORY_EXISTING)).defaultsTo(Paths.get("."));
        parser.allowsUnrecognizedOptions();
        final OptionSet optionSet = parser.parse(args);

        final Path gameDirectory = optionSet.valueOf(gameDir);
        Main.pluginEnvironment = new PluginEnvironment();
        final String implementationVersion = PluginEnvironment.class.getPackage().getImplementationVersion();
        Main.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.VERSION, () -> implementationVersion == null ? "dev" : implementationVersion);
        Main.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.BASE_DIRECTORY, () -> gameDirectory);
        // TODO Read in plugin directories from CLI/Config
        Main.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.PLUGIN_DIRECTORIES, () -> Arrays.asList(gameDirectory.resolve("mods"), gameDirectory.resolve("plugins")));

        final ArgumentList lst = ArgumentList.from(args);
        Launcher.main(lst.getArguments());
    }

    public static PluginEnvironment getLaunchPluginEnvironment() {
        return Main.pluginEnvironment;
    }
}
