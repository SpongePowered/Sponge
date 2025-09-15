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
package org.spongepowered.common.applaunch.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All options used very early during Sponge initialization when Configurate is not yet available.
 */
public record LaunchConfig(
    String args,
    String additionalPluginsDirectory,
    String librariesDirectory, boolean autoDownloadLibraries, boolean checkLibraryHashes
) {
    public static final LaunchConfig DEFAULT = new LaunchConfig("--nogui", "${MODS_DIR}/plugins", "${BASE_DIR}/libraries", true, true);

    public Properties toProperties() {
        final Properties props = new Properties();
        props.put("args", this.args);
        props.put("additional-plugins-directory", this.additionalPluginsDirectory);
        props.put("libraries-directory", this.librariesDirectory);
        props.put("auto-download-libraries", Boolean.toString(this.autoDownloadLibraries));
        props.put("check-library-hashes", Boolean.toString(this.checkLibraryHashes));
        return props;
    }

    public LaunchConfig withProperties(final Properties props) {
        return new LaunchConfig(
            LaunchConfig.getString(props, "args", this.args),
            LaunchConfig.getString(props, "additional-plugins-directory", this.additionalPluginsDirectory),
            LaunchConfig.getString(props, "libraries-directory", this.librariesDirectory),
            LaunchConfig.getBoolean(props, "auto-download-libraries", this.autoDownloadLibraries),
            LaunchConfig.getBoolean(props, "check-library-hashes", this.checkLibraryHashes)
        );
    }

    public LaunchConfig withAdditionalPluginsDirectory(final String additionalPluginsDirectory) {
        return new LaunchConfig(this.args, additionalPluginsDirectory, this.librariesDirectory, this.autoDownloadLibraries, this.checkLibraryHashes);
    }

    private static String getString(final Properties props, String key, String defaultValue) {
        final Object value = props.get(key);
        return value == null ? defaultValue : value.toString();
    }

    private static boolean getBoolean(final Properties props, String key, boolean defaultValue) {
        final Object value = props.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value.toString());
    }

    public static LaunchConfig load(final Path baseDir, final boolean convertLegacy) throws IOException {
        final Path configDir = baseDir.resolve("config/sponge");
        final Path launchConfigFile = configDir.resolve("launch.properties");

        if (Files.exists(launchConfigFile)) {
            final Properties props = new Properties();
            try (final BufferedReader reader = Files.newBufferedReader(launchConfigFile)) {
                props.load(reader);
            }
            return LaunchConfig.DEFAULT.withProperties(props);
        }

        LaunchConfig launchConfig = LaunchConfig.DEFAULT;

        final Path launcherConfigFile = baseDir.resolve("launcher.conf");
        if (convertLegacy) {
            // legacy plugin directory option
            final Path commonConfigFile = configDir.resolve("sponge.conf");
            if (Files.exists(commonConfigFile)) {
                final Matcher matcher = Pattern.compile("plugins-dir=\"(.*)\"").matcher(Files.readString(commonConfigFile));
                if (matcher.find()) {
                    launchConfig = launchConfig.withAdditionalPluginsDirectory(LaunchConfig.convertLegacyPath(matcher.group(1)));
                }
            }

            // legacy installer options
            if (Files.exists(launcherConfigFile)) {
                final Properties props = new Properties();
                try (final BufferedReader reader = Files.newBufferedReader(launcherConfigFile)) {
                    props.load(reader);
                }
                for (final var entry : props.entrySet()) {
                    if (entry.getValue() instanceof String value) {
                        entry.setValue(value.replace("\"", "").replace("BASE_DIRECTORY", "BASE_DIR"));
                    }
                }
                launchConfig = launchConfig.withProperties(props);
            }
        }

        Files.createDirectories(configDir);

        try (final BufferedWriter writer = Files.newBufferedWriter(launchConfigFile)) {
            launchConfig.toProperties().store(writer, "Sponge launch configuration");
        }

        if (convertLegacy) {
            Files.deleteIfExists(launcherConfigFile);
        }

        return launchConfig;
    }

    public static String convertLegacyPath(final String value) {
        return value
            .replace("${CANONICAL_GAME_DIR}", "${BASE_DIR}")
            .replace("${CANONICAL_MODS_DIR}", "${MODS_DIR}")
            .replace("${CANONICAL_CONFIG_DIR}", "${CONFIG_DIR}");
    }
}
