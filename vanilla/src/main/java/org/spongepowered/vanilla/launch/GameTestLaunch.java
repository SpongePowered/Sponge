/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.launch;

import com.google.inject.Stage;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Runs Minecraft's headless {@code GameTestServer} with Sponge loaded. Tests and
 * their registration come from the {@code gametest} source sets on the classpath.
 */
public final class GameTestLaunch extends VanillaLaunch {

    private GameTestLaunch(final VanillaPluginPlatform pluginPlatform) {
        super(pluginPlatform, Stage.DEVELOPMENT);
    }

    public static void launch(final VanillaPluginPlatform pluginPlatform, final String[] args) {
        final GameTestLaunch launcher = new GameTestLaunch(pluginPlatform);
        Launch.setInstance(launcher);
        launcher.launchPlatform(args);
    }

    @Override
    public boolean dedicatedServer() {
        return true;
    }

    @Override
    protected void performBootstrap(final String[] args) {
        VanillaBootstrap.perform("GameTest Server", () -> {
            try {
                GameTestLaunch.runGameTestServer(args);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // What GameTestMainUtil does from 1.21.5; until then vanilla has no entrypoint for the game test server
    private static void runGameTestServer(final String[] args) throws Exception {
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        final OptionSpec<String> universe = parser.accepts("universe", "Where the test world is created, replacing anything there")
            .withRequiredArg()
            .defaultsTo("gametestserver");
        final OptionSpec<File> report = parser.accepts("report", "Exports results in a junit-like XML report at the given path")
            .withRequiredArg()
            .ofType(File.class)
            .defaultsTo(new File("gametest-report.xml"));
        final OptionSpec<String> tests = parser.accepts("tests", "Which tests to run by name, with * as a wildcard, e.g. piston.*")
            .withRequiredArg();
        final OptionSet options = parser.parse(args);

        GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(options.valueOf(report)));
        Bootstrap.bootStrap();
        Util.startTimerHackThread();

        final Path directory = Paths.get(options.valueOf(universe));
        if (Files.exists(directory)) {
            FileUtils.deleteDirectory(directory.toFile());
        }
        Files.createDirectories(directory);
        final LevelStorageSource.LevelStorageAccess access = LevelStorageSource.createDefault(directory).createAccess("gametestworld");
        // As MainMixin does for the dedicated server, which this skips
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();
        final PackRepository packs = ServerPacksSource.createPackRepository(access);
        final Collection<TestFunction> functions = options.has(tests)
            ? GameTestLaunch.select(options.valueOf(tests))
            : GameTestRegistry.getAllTestFunctions();
        MinecraftServer.spin(thread -> GameTestServer.create(thread, access, packs, functions, BlockPos.ZERO));
    }

    private static Collection<TestFunction> select(final String selection) {
        final Pattern pattern = Pattern.compile(Pattern.quote(selection).replace("*", "\\E.*\\Q"));
        return GameTestRegistry.getAllTestFunctions().stream()
            .filter(function -> pattern.matcher(function.testName()).matches())
            .toList();
    }
}
