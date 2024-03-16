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
package org.spongepowered.common.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * The absolute minimum required to have a plugin platform to function
 * with regard to SpongeCommon's configurations being usable. Required
 * to be used if the {@link org.spongepowered.common.event.tracking.PhaseTracker}
 * is being utilized since the configurations are being checked.
 */
public class TestPluginPlatform implements PluginPlatform {

    private static final Logger LOGGER = LogManager.getLogger("UnitTestPlatform");

    private final Path outputDirectory;
    private final Path pluginDirectory;

    public TestPluginPlatform() {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final String directory = classLoader.getResource(".").getFile();
        final Path p = Path.of(directory);
        this.outputDirectory = p;
        this.pluginDirectory = p.resolve("plugins");
    }

    @Override
    public String version() {
        return "unit-test";
    }

    @Override
    public void setVersion(final String version) {

    }

    @Override
    public Logger logger() {
        return TestPluginPlatform.LOGGER;
    }

    @Override
    public Path baseDirectory() {
        return this.outputDirectory;
    }

    @Override
    public void setBaseDirectory(final Path baseDirectory) {

    }

    @Override
    public List<Path> pluginDirectories() {
        return Collections.singletonList(this.pluginDirectory);
    }

    @Override
    public void setPluginDirectories(final List<Path> pluginDirectories) {

    }

    @Override
    public String metadataFilePath() {
        return PluginPlatformConstants.METADATA_FILE_LOCATION;
    }

    @Override
    public void setMetadataFilePath(final String metadataFilePath) {

    }
}
